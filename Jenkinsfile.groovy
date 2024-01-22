pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'index.docker.io'
        APP_IMAGE_NAME = 'examor/app'
        SERVER_IMAGE_NAME = 'examor/server'
        DATABASE_IMAGE_NAME = 'examor/database'
        IMAGE_TAG = 'latest'
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
