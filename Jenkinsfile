pipeline {
    agent any

    environment {
        REGISTRY = "docker.io/chanathipcha24"
        IMAGE_NAME = "rentora-api"
        BUILD_VERSION = "${env.BUILD_NUMBER}"
        KUBE_DEPLOYMENT = "k8s/backend-deployment.yaml"
    }

    stages {
        stage('Checkout Backend') {
            steps {
                checkout scm
            }
        }

        stage('Run Gradle Tests') {
            steps {
                echo "Running Spring Boot tests..."
                sh """
                    chmod +x gradlew
                    ./gradlew clean test --no-daemon
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image..."
                sh """
                    docker build -t $REGISTRY/$IMAGE_NAME:${BUILD_VERSION} .
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-cred', 
                    usernameVariable: 'DOCKER_USER', 
                    passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push $REGISTRY/$IMAGE_NAME:${BUILD_VERSION}
                    """
                }
            }
        }

        stage('Create/Update Kubernetes Secret') {
            steps {
                withCredentials([
                    string(credentialsId: 'RENTORA_SERVER_PORT', variable: 'SERVER_PORT'),
                    string(credentialsId: 'RENTORA_POSTGRES_DB', variable: 'POSTGRES_DB'),
                    string(credentialsId: 'RENTORA_POSTGRES_USER', variable: 'POSTGRES_USER'),
                    string(credentialsId: 'RENTORA_POSTGRES_PASSWORD', variable: 'POSTGRES_PASSWORD'),
                    string(credentialsId: 'RENTORA_SPRING_DATASOURCE_HOST', variable: 'SPRING_DATASOURCE_HOST'),
                    string(credentialsId: 'RENTORA_JWT_SECRET', variable: 'JWT_SECRET'),
                    string(credentialsId: 'RENTORA_AWS_S3_ACCESS_KEY', variable: 'AWS_S3_ACCESS_KEY'),
                    string(credentialsId: 'RENTORA_AWS_S3_SECRET_ACCESS_KEY', variable: 'AWS_S3_SECRET_ACCESS_KEY'),
                    string(credentialsId: 'RENTORA_AWS_S3_BUCKET_NAME', variable: 'AWS_S3_BUCKET_NAME'),
                    string(credentialsId: 'RENTORA_AWS_S3_REGION', variable: 'AWS_S3_REGION'),
                    string(credentialsId: 'RENTORA_ELASTICSEARCH_URI', variable: 'ELASTICSEARCH_URI'),
                    string(credentialsId: 'RENTORA_APP_CORS_ALLOWED_ORIGINS', variable: 'APP_CORS_ALLOWED_ORIGINS')
                ]) {
                    sh """
                        microk8s kubectl delete secret rentora-backend-env --ignore-not-found
                        microk8s kubectl create secret generic rentora-backend-env \\
                            --from-literal=SERVER_PORT=$SERVER_PORT \\
                            --from-literal=POSTGRES_DB=$POSTGRES_DB \\
                            --from-literal=POSTGRES_USER=$POSTGRES_USER \\
                            --from-literal=POSTGRES_PASSWORD=$POSTGRES_PASSWORD \\
                            --from-literal=SPRING_DATASOURCE_HOST=$SPRING_DATASOURCE_HOST \\
                            --from-literal=JWT_SECRET=$JWT_SECRET \\
                            --from-literal=AWS_S3_ACCESS_KEY=$AWS_S3_ACCESS_KEY \\
                            --from-literal=AWS_S3_SECRET_ACCESS_KEY=$AWS_S3_SECRET_ACCESS_KEY \\
                            --from-literal=AWS_S3_BUCKET_NAME=$AWS_S3_BUCKET_NAME \\
                            --from-literal=AWS_S3_REGION=$AWS_S3_REGION \\
                            --from-literal=ELASTICSEARCH_URI=$ELASTICSEARCH_URI \\
                            --from-literal=APP_CORS_ALLOWED_ORIGINS=$APP_CORS_ALLOWED_ORIGINS
                    """
                }
            }
        }

        stage('Deploy to MicroK8s') {
            steps {
                echo "Updating Kubernetes Deployment image..."
                sh """
                    microk8s kubectl set image deployment/rentora-backend rentora-backend=$REGISTRY/$IMAGE_NAME:${BUILD_VERSION} --record
                """
            }
        }

        stage('Verify Deployment') {
            steps {
                sh """
                    microk8s kubectl get pods
                    microk8s kubectl get svc
                """
            }
        }
    }

    post {
        always {
            echo "Cleaning up Docker images..."
            sh "docker image prune -f"
        }
    }
}