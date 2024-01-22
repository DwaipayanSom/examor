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
                    docker.build("-t ${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${IMAGE_TAG} ./examor/app")
                    docker.build("-t ${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${IMAGE_TAG} ./examor/server")
                    docker.build("-t ${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG} ./examor/database")
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                script {
                    sh "kubectl apply -f 'Kubernetes Files/app-deployment.yaml'"
                    sh "kubectl apply -f 'Kubernetes Files/app-service.yaml'"
                    sh "kubectl apply -f 'Kubernetes Files/server-deployment.yaml'"
                    sh "kubectl apply -f 'Kubernetes Files/server-service.yaml'"
                    sh "kubectl apply -f 'Kubernetes Files/database-deployment.yaml'"
                    sh "kubectl apply -f 'Kubernetes Files/database-service.yaml'"

                    sh 'sleep 15'
                }
            }
        }
    }

    post {
        always {
            script {
                docker.image("${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${IMAGE_TAG}").remove()
                docker.image("${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${IMAGE_TAG}").remove()
                docker.image("${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG}").remove()
            }
        }
    }
}
