pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
                echo 'Repository cloned successfully'
            }
        }

        stage('Build') {
            steps {
                echo 'Building Spring Boot application...'
                sh '''
                    pwd
                    ls -la
                    chmod +x mvnw
                    ./mvnw clean compile -DskipTests
                '''
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh '''
                    docker build -t entityrail-backend:${BUILD_NUMBER} .
                    docker tag entityrail-backend:${BUILD_NUMBER} entityrail-backend:latest
                '''
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production...'
                sh '''
                    cd /opt/entityrail
                    docker-compose stop backend || true
                    docker-compose rm -f backend || true
                    docker-compose up -d backend
                '''
            }
        }

        stage('Verify') {
            steps {
                echo 'Verifying deployment...'
                sh '''
                    echo "Waiting for backend to start..."
                    sleep 15
                    for i in 1 2 3 4 5; do
                        if curl -f http://localhost:8081/actuator/health; then
                            echo "Backend is healthy!"
                            exit 0
                        fi
                        echo "Attempt $i failed, waiting..."
                        sleep 5
                    done
                    echo "Backend failed to start"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
            echo "Backend API: https://api.entityrail.com"
            echo "Swagger UI: https://api.entityrail.com/swagger-ui/index.html"
        }
        failure {
            echo 'Pipeline failed!'
            echo "Check logs at: ${env.BUILD_URL}/console"
        }
    }
}