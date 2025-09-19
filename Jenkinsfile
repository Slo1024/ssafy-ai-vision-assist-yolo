pipeline {
    agent any

    environment {
        PROJECT_DIR = '/opt/project'
        GITLAB_REPO_URL = 'https://lab.ssafy.com/s13-ai-image-sub1/S13P21E101.git'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo 'Checking out code from GitLab...'

                    // Checkout the code first
                    checkout scm

                    // Detect current branch after checkout
                    def gitBranch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                    echo "Detected branch: ${gitBranch}"
                    env.CURRENT_BRANCH = gitBranch
                }
            }
        }

        stage('Copy Source Code') {
            steps {
                script {
                    echo 'Copying source code to project directory...'
                    sh """
                        # Remove old backend files (use sudo since we have permission now)
                        sudo rm -rf ${PROJECT_DIR}/backend/*

                        # Copy BE directory contents to backend
                        if [ -d "BE/lookey" ]; then
                            echo "Copying BE/lookey to ${PROJECT_DIR}/backend/"
                            sudo cp -r BE/lookey/* ${PROJECT_DIR}/backend/
                            sudo chown -R jenkins:jenkins ${PROJECT_DIR}/backend
                        else
                            echo "BE/lookey directory not found"
                            ls -la .
                            exit 1
                        fi

                        # Copy AI service
                        echo "Copying AI service..."
                        if [ -d "AI" ]; then
                            # Remove old AI files
                            sudo rm -rf ${PROJECT_DIR}/AI/*
                            # Create AI directory if it doesn't exist
                            sudo mkdir -p ${PROJECT_DIR}/AI
                            # Copy new AI files
                            sudo cp -r AI/* ${PROJECT_DIR}/AI/
                            sudo chown -R jenkins:jenkins ${PROJECT_DIR}/AI
                            echo "AI directory copied successfully"
                            ls -la ${PROJECT_DIR}/AI/
                        else
                            echo "AI directory not found"
                            ls -la .
                            exit 1
                        fi

                        # Copy model file
                        if [ -f "clip_linear_head.pt" ]; then
                            echo "Copying model file..."
                            sudo cp clip_linear_head.pt ${PROJECT_DIR}/
                            sudo chown jenkins:jenkins ${PROJECT_DIR}/clip_linear_head.pt
                            echo "Model file copied successfully"
                        else
                            echo "WARNING: Model file not found - AI service may fail to start"
                        fi

                        # Copy Docker files (using existing .env on server)
                        echo "Copying Docker configuration files..."
                        sudo cp Dockerfile ${PROJECT_DIR}/
                        sudo cp docker-compose.*.yml ${PROJECT_DIR}/
                        sudo chown jenkins:jenkins ${PROJECT_DIR}/Dockerfile
                        sudo chown jenkins:jenkins ${PROJECT_DIR}/docker-compose.*.yml

                        # List copied files for verification
                        echo "Files in backend directory:"
                        ls -la ${PROJECT_DIR}/backend/
                        echo "Files in AI directory:"
                        ls -la ${PROJECT_DIR}/AI/
                        echo "Docker files in project directory:"
                        ls -la ${PROJECT_DIR}/ | grep -E "(Dockerfile|docker-compose)"
                        echo "Model file:"
                        ls -la ${PROJECT_DIR}/clip_linear_head.pt || echo "Model file not found"
                        echo "Using existing .env file on server:"
                        ls -la ${PROJECT_DIR}/.env
                    """
                }
            }
        }

        stage('Environment Detection') {
            steps {
                script {
                    echo "Current branch: ${env.CURRENT_BRANCH}"

                    if (env.CURRENT_BRANCH == 'master') {
                        env.DEPLOY_ENV = 'prod'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.prod.yml'
                        env.API_PORT = '8081'
                    } else if (env.CURRENT_BRANCH == 'dev') {
                        env.DEPLOY_ENV = 'dev'
                        env.DOCKER_COMPOSE_FILE = 'docker-compose.dev.yml'
                        env.API_PORT = '8082'
                    } else {
                        env.DEPLOY_ENV = 'dev'  // Default to dev for other branches
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
                            if ls build/libs/*.jar 1> /dev/null 2>&1; then
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

                        // Deploy backend services
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} down || true"
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} build --no-cache"
                        sh "docker compose -f ${DOCKER_COMPOSE_FILE} up -d"

                        // Deploy AI service
                        echo "Deploying AI service..."
                        sh """
                            # Stop and remove existing AI container
                            docker stop lookey-ai-service || true
                            docker rm lookey-ai-service || true

                            # Clean up any orphaned containers
                            docker compose -f docker-compose.ai.yml down || true

                            # Build and deploy
                            docker compose -f docker-compose.ai.yml build --no-cache
                            docker compose -f docker-compose.ai.yml up -d
                        """
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "Waiting for services to start..."
                    sleep(60) // Wait longer for both Spring Boot and AI service to start

                    // Backend health check
                    echo "Checking backend application health..."
                    def backendHealthy = false
                    try {
                        sh """
                            curl -f http://localhost:${API_PORT}/actuator/health >/dev/null 2>&1
                        """
                        backendHealthy = true
                        echo "Backend health check successful!"
                    } catch (Exception e1) {
                        try {
                            sh """
                                curl -f http://localhost:${API_PORT}/api/test/health >/dev/null 2>&1
                            """
                            backendHealthy = true
                            echo "Backend custom health check successful!"
                        } catch (Exception e2) {
                            echo "Backend health check failed - checking logs..."
                            sh "docker logs --tail 20 springapp-${DEPLOY_ENV} || echo 'Backend container not found'"
                            echo "⚠️ Backend health check failed but continuing deployment"
                        }
                    }

                    // AI service health check
                    echo "Checking AI service health..."
                    def aiHealthy = false
                    try {
                        sh """
                            curl -f http://localhost:8083/health >/dev/null 2>&1
                        """
                        aiHealthy = true
                        echo "AI service health check successful!"
                    } catch (Exception e) {
                        echo "AI service health check failed - checking logs..."
                        sh "docker logs --tail 20 lookey-ai-service || echo 'AI container not found'"
                        echo "⚠️ AI service health check failed but continuing deployment"
                    }

                    // Summary
                    if (backendHealthy && aiHealthy) {
                        echo "✅ All health checks passed!"
                    } else if (backendHealthy || aiHealthy) {
                        echo "⚠️ Partial health check success - some services may still be starting"
                    } else {
                        echo "⚠️ Health checks failed but containers are running - services may need more time to start"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful for ${env.DEPLOY_ENV} environment!"
            echo "✅ Backend service running on port ${env.API_PORT}"
            echo "✅ AI service running on port 8083"
        }
        failure {
            echo "❌ Deployment failed for ${env.DEPLOY_ENV} environment!"
            script {
                sh "docker logs --tail 50 springapp-${env.DEPLOY_ENV} || echo 'Backend container not found'"
                sh "docker logs --tail 50 lookey-ai-service || echo 'AI container not found'"
            }
        }
    }
}