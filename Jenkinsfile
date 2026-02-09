pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
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
                    sh 'DOCKER_BUILDKIT=1 docker compose -f docker-compose.prod.yml build app'
                    sh 'docker compose -f docker-compose.prod.yml up -d --force-recreate'
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
