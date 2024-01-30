pipeline {
    agent any

    environment {
        APP_IMAGE_NAME = 'examor-app'
        SERVER_IMAGE_NAME = 'examor-server'
        DATABASE_IMAGE_NAME = 'examor-database'
        IMAGE_TAG = 'latest'
    }

    stages {
        stage("Cloning the Repository") {
            steps {
                git url: "https://github.com/DwaipayanSom/examor.git", branch: "main"
            }
        }

        stage('Building Docker Images') {
            steps {
                script {
                    sh 'docker build ./app -t ${APP_IMAGE_NAME}:${IMAGE_TAG}'
                    sh 'docker build ./server -t ${SERVER_IMAGE_NAME}:${IMAGE_TAG}'
                    sh 'docker build ./database -t ${DATABASE_IMAGE_NAME}:${IMAGE_TAG}'
                }
            }
        }

       stage('Login and Push Image') {
            steps {
                withCredentials([usernamePassword(credentialsId:'dockerhub',passwordVariable:'dockerhub_password',usernameVariable:'dockerhub_username')]){
                    sh "docker tag ${APP_IMAGE_NAME} ${env.dockerhub_username}/${APP_IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker tag ${SERVER_IMAGE_NAME} ${env.dockerhub_username}/${SERVER_IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker tag ${DATABASE_IMAGE_NAME} ${env.dockerhub_username}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG}"
                    
                    sh "docker login -u ${env.dockerhub_username} -p ${env.dockerhub_password}"

                    sh "docker push ${env.dockerhub_username}/${APP_IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${env.dockerhub_username}/${SERVER_IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${env.dockerhub_username}/${DATABASE_IMAGE_NAME}:${IMAGE_TAG}"
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
