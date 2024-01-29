pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'index.docker.io'
        APP_IMAGE_NAME = 'examor/app'
        SERVER_IMAGE_NAME = 'examor/server'
        DATABASE_IMAGE_NAME = 'examor/database'
        IMAGE_TAG = 'latest'
        NETWORK_NAME = 'examor-network'
    }

    stages {
        stage("Cloning the Repository") {
            steps {
                git url: "https://github.com/DwaipayanSom/examor.git", branch: "main"
            }
        }

        stage('Building and Loading the Docker Images') {
            steps {
                script {
                    docker.build("${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${IMAGE_TAG}", "./app")
                    docker.build("${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${IMAGE_TAG}", "./server")
                    docker.build("${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG}", "./database")
                }
            }
        }

        stage('Create Docker Network') {
            steps {
                script {
                    def existingNetwork = sh(script: "docker network ls -q -f name=${NETWORK_NAME}", returnStatus: true).toString().trim()
                    
                    if (existingNetwork.isEmpty()) {
                        sh "docker network create ${NETWORK_NAME}"
                    } else {
                        echo "Network ${NETWORK_NAME} already exists."
                    }
                }
            }
        }

        stage('Run Containers in the Network') {
        steps {
            script {
                def databaseContainerExists = sh(script: "docker ps -q -f name=database", returnStatus: true).toString().trim()
                def serverContainerExists = sh(script: "docker ps -q -f name=server", returnStatus: true).toString().trim()
                def appContainerExists = sh(script: "docker ps -q -f name=app", returnStatus: true).toString().trim()

                if (databaseContainerExists) {
                sh "docker stop database"
                sh "docker rm database"
                }

                if (serverContainerExists) {
                sh "docker stop server"
                sh "docker rm server"
                }

                if (appContainerExists) {
                sh "docker stop app"
                sh "docker rm app"
                }

                sh "docker run -d --network ${NETWORK_NAME} -p 52020:3306 --name database ${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker run -d --network ${NETWORK_NAME} -p 51717:51717 --name server ${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker run -d --network ${NETWORK_NAME} -p 51818:51818 --name app ${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${IMAGE_TAG} /bin/sh -c 'sleep 15 && pnpm dev:docker'"
            }
        }
    }

        
        stage('Deploy to Minikube') {
            steps {
                script {
                    def kubeconfigPath = '/home/linux_retrinex/.kube/config'
                    def kubectlCmd = "/usr/local/bin/kubectl --kubeconfig=${kubeconfigPath}"

                    sh "${kubectlCmd} apply -f 'Kubernetes Files/app-deployment.yaml'"
                    sh "${kubectlCmd} apply -f 'Kubernetes Files/app-service.yaml'"
                    sh "${kubectlCmd} apply -f 'Kubernetes Files/server-deployment.yaml'"
                    sh "${kubectlCmd} apply -f 'Kubernetes Files/server-service.yaml'"
                    sh "${kubectlCmd} apply -f 'Kubernetes Files/database-deployment.yaml'"
                    sh "${kubectlCmd} apply -f 'Kubernetes Files/database-service.yaml'"

                    sh 'sleep 15'
                }
            }
        }
    }
}
