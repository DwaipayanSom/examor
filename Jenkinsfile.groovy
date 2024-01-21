pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'localhost:5000'
        APP_IMAGE_NAME = 'examor/app'
        SERVER_IMAGE_NAME = 'examor/server'
        DATABASE_IMAGE_NAME = 'examor/database'
    }

    stages {
        stage('Build and Load Docker Images') {
            steps {
                script {
                    docker.build("-t ${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${BUILD_NUMBER} ./examor/app")
                    docker.build("-t ${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${BUILD_NUMBER} ./examor/server")
                    docker.build("-t ${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${BUILD_NUMBER} ./examor/database")
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                script {
                    sh 'kubectl apply -f kubernetes/app-deployment.yaml'
                    sh 'kubectl apply -f kubernetes/app-service.yaml'
                    sh 'kubectl apply -f kubernetes/server-deployment.yaml'
                    sh 'kubectl apply -f kubernetes/server-service.yaml'
                    sh 'kubectl apply -f kubernetes/database-deployment.yaml'
                    sh 'kubectl apply -f kubernetes/database-service.yaml'

                    // Sleep for 15 seconds to simulate the delay
                    sh 'sleep 15'
                }
            }
        }
    }

    post {
        always {
            script {
                docker.image("${DOCKER_REGISTRY}/${APP_IMAGE_NAME}:${BUILD_NUMBER}").remove()
                docker.image("${DOCKER_REGISTRY}/${SERVER_IMAGE_NAME}:${BUILD_NUMBER}").remove()
                docker.image("${DOCKER_REGISTRY}/${DATABASE_IMAGE_NAME}:${BUILD_NUMBER}").remove()
            }
        }
    }
}
