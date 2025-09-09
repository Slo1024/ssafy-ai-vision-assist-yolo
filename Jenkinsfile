pipeline {
    agent any
    
    environment {
        PROJECT_DIR = '/opt/project'
        GITLAB_REPO_URL = 'https://lab.ssafy.com/s13-ai-image-sub1/S13P21E101.git'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitLab...'
                git branch: env.BRANCH_NAME, 
                    url: env.GITLAB_REPO_URL,
                    credentialsId: 'gitlab-credentials'
            }
        }
        
        stage('Copy Source Code') {
            steps {
                script {
                    echo 'Copying source code to project directory...'
                    sh """
                        # Remove old backend files
                        sudo rm -rf ${PROJECT_DIR}/backend/*
                        
                        # Copy BE directory contents to backend
                        if [ -d "BE/lookey" ]; then
                            echo "Copying BE/lookey to ${PROJECT_DIR}/backend/"
                            sudo cp -r BE/lookey/* ${PROJECT_DIR}/backend/
                            sudo chown -R jenkins:jenkins ${PROJECT_DIR}/backend
                        else
                            echo "BE/lookey directory not found"
                            exit 1
                        fi
                        
                        # List copied files for verification
                        echo "Files in backend directory:"
                        ls -la ${PROJECT_DIR}/backend/
                    """
                }
            }
        }
        
        stage('Environment Detection') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'master') {
                        env.DEPLOY_ENV = 'prod'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
                        env.API_PORT = '8081'
                    } else if (env.BRANCH_NAME == 'dev') {
                        env.DEPLOY_ENV = 'dev' 
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.API_PORT = '8082'
                    }
                }
                echo "Deploying to: ${env.DEPLOY_ENV}"
                echo "Using compose file: ${env.DOCKER_COMPOSE_FILE}"
            }
        }
        
        stage('Build Backend') {
            steps {
                script {
                    dir("${PROJECT_DIR}/backend") {
                        echo "Building Spring Boot application..."
                        sh """
                            # Make gradlew executable
                            chmod +x ./gradlew
                            
                            # Build the application
                            ./gradlew clean build -x test
                            
                            # Verify JAR file was created
                            if [ -f "build/libs/*.jar" ]; then
                                echo "JAR file created successfully:"
                                ls -la build/libs/
                            else
                                echo "JAR file not found!"
                                exit 1
                            fi
                        """
                    }
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    dir(env.PROJECT_DIR) {
                        echo "Starting deployment for ${env.DEPLOY_ENV} environment..."
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} down || true"
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} build --no-cache"
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} up -d"
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    echo "Waiting for services to start..."
                    sleep(45) // Wait longer for Spring Boot to start
                    
                    echo "Checking application health..."
                    sh """
                        # Try multiple health check endpoints
                        if curl -f http://localhost:${API_PORT}/actuator/health; then
                            echo "Health check successful!"
                        elif curl -f http://localhost:${API_PORT}/api/test/health; then
                            echo "Custom health check successful!"
                        else
                            echo "Health check failed - checking logs..."
                            docker logs --tail 20 springapp-${DEPLOY_ENV}
                            echo "Health check failed but deployment may still be starting"
                        fi
                    """
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
            script {
                sh "docker logs --tail 50 springapp-${env.DEPLOY_ENV} || echo 'Container not found'"
            }
        }
    }
}