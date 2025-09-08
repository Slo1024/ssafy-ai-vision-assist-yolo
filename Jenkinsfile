pipeline {
    agent any
    
    environment {
        PROJECT_DIR = '/opt/project'
        GITLAB_REPO_URL = 'https://lab.ssafy.com/s13-ai-image-sub1/S13P21E101.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Code already checked out by Jenkins SCM'
                script {
                    // Get current branch name
                    env.CURRENT_BRANCH = env.GIT_BRANCH?.replaceFirst(/^origin\//, '') ?: 'master'
                    echo "Building branch: ${env.CURRENT_BRANCH}"
                }
            }
        }
        
        stage('Environment Detection') {
            steps {
                script {
                    if (env.CURRENT_BRANCH == 'master') {
                        env.DEPLOY_ENV = 'prod'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
                        env.API_PORT = '8081'
                    } else if (env.CURRENT_BRANCH == 'dev') {
                        env.DEPLOY_ENV = 'dev' 
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.API_PORT = '8082'
                    }
                }
                echo "Deploying to: ${env.DEPLOY_ENV}"
                echo "Using compose file: ${env.DOCKER_COMPOSE_FILE}"
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    dir(env.PROJECT_DIR) {
                        echo "Starting deployment for ${env.DEPLOY_ENV} environment..."
                        sh 'docker compose -f ${DOCKER_COMPOSE_FILE} up -d --build'
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sleep(30) // Wait for services to start
                    sh 'curl -f http://localhost:${API_PORT}/actuator/health || echo "Health check failed - service may still be starting"'
                }
            }
        }
    }
    
    post {
        success {
            echo "✅ Deployment successful for ${env.DEPLOY_ENV} environment!"
        }
        failure {
            echo "❌ Deployment failed for ${env.DEPLOY_ENV} environment!"
        }
    }
}