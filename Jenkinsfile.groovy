pipeline{
    agent any
    stages{
        stage('Clone'){
            steps{
                git url: 'https://github.com/DwaipayanSom/examor.git', branch: 'master'
            }
        }
        stage('Build and Test'){
            steps{
                sh 'docker build . -t examor:latest'
            }
        }
        stage('Login and Push Image'){
            steps{
                withCredentials([usernamePassword(credentialsId:'dockerhub_credentials',passwordVariable:'dockerhub_password',usernameVariable:'dockerhub_username')]){
                    sh "docker login -u ${env.dockerhub_username} -p ${env.dockerhub_password}"
                    sh "docker push dwaipayansom/node-todo-app-cicd:latest"
                }
            }
        }
        stage('Deploy'){
            steps{
                sh 'docker-compose down && docker-compose up -d'
            }
        }
    }
}

