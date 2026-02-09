pipeline {
    agent any

    environment {
        NETWORK_NAME = 'swyp-network'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Ensure Network Exists') {
            steps {
                sh "docker network inspect ${NETWORK_NAME} >/dev/null 2>&1 || docker network create ${NETWORK_NAME}"
            }
        }

        stage('Build & Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'DB_ROOT_PASSWORD', variable: 'DB_ROOT_PASSWORD'),
                    string(credentialsId: 'DB_USER', variable: 'DB_USER'),
                    string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD'),
                    string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                    string(credentialsId: 'KAKAO_CLIENT_ID', variable: 'KAKAO_CLIENT_ID'),
                    string(credentialsId: 'KAKAO_CLIENT_SECRET', variable: 'KAKAO_CLIENT_SECRET'),
                    string(credentialsId: 'KAKAO_REDIRECT_URI', variable: 'KAKAO_REDIRECT_URI'),
                    string(credentialsId: 'GOOGLE_GENAI_API_KEY', variable: 'GOOGLE_GENAI_API_KEY'),
                    string(credentialsId: 'EXHIBITION_API_BASE_URL', variable: 'EXHIBITION_API_BASE_URL'),
                    string(credentialsId: 'EXHIBITION_API_SERVICE_KEY', variable: 'EXHIBITION_API_SERVICE_KEY'),
                    string(credentialsId: 'OPENAI_API_KEY', variable: 'OPENAI_API_KEY'),
                    string(credentialsId: 'ADMIN_PASSWORD', variable: 'ADMIN_PASSWORD')
                ]) {
                    sh 'docker compose -f docker-compose.prod.yml build --no-cache app'
                    sh 'docker compose -f docker-compose.prod.yml up -d app'
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker image prune -af'
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
