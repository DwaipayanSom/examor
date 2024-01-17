pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_VERSION = '3'
        DOCKER_APP_IMAGE = 'examor/app:latest'
        DOCKER_SERVER_IMAGE = 'examor/server:latest'
        DOCKER_DATABASE_IMAGE = 'examor/database:latest'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Push App Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.example.com', 'docker-hub-credentials') {
                        docker.build(DOCKER_APP_IMAGE, './app/')
                        docker.image(DOCKER_APP_IMAGE).push()
                    }
                }
            }
        }

        stage('Build and Push Server Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.example.com', 'docker-hub-credentials') {
                        docker.build(DOCKER_SERVER_IMAGE, './server/')
                        docker.image(DOCKER_SERVER_IMAGE).push()
                    }
                }
            }
        }

        stage('Build and Push Database Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.example.com', 'docker-hub-credentials') {
                        docker.build(DOCKER_DATABASE_IMAGE, './database/')
                        docker.image(DOCKER_DATABASE_IMAGE).push()
                    }
                }
            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                script {
                    sh "docker-compose -f docker-compose.yml up -d"
                }
            }
        }

        stage('Run Tests') {
            steps {
                // Add your test steps here
            }
        }
    }

    post {
        always {
            script {
                sh "docker-compose -f docker-compose.yml down"
            }
        }
        success {
            echo 'Build and deployment successful!'
        }
        failure {
            echo 'Build or deployment failed!'
        }
    }
}
