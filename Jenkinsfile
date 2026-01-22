pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9-eclipse-temurin-21
    command: ["sleep"]
    args: ["infinity"]
  - name: k8s-tools
    image: alpine/k8s:1.29.2
    command: ["sleep"]
    args: ["infinity"]
'''
        }
    }

    environment {
        // 1. ADD YOUR REGISTRY URL HERE
        IMAGE_NAME = "notification-service"
        // Use the ID you created in Jenkins (acr-creds)
        DOCKER = credentials('docker-creds')
    }

    stages {
        stage('Build & Push') {
            steps {
                sh "git clone https://github.com/Weather-alert/weather-alert.git parent-folder"

                // 2. Install it to the local .m2 cache
                dir('parent-folder') {
                    container('maven'){
                        sh "mvn install -N"
                    }
                }
                container('maven') {

                    echo "Building Quarkus App and Pushing to ACR..."
                    sh """
                    ./mvnw clean package \
                      -Dquarkus.container-image.build=true \
                      -Dquarkus.container-image.push=true \
                      -Dquarkus.container-image.builder=jib \
                      -Dquarkus.container-image.group=blaz040 \
                      -Dquarkus.container-image.name=${IMAGE_NAME} \
                      -Dquarkus.container-image.username=${DOCKER_USR} \
                      -Dquarkus.container-image.password=${DOCKER_PSW}
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                container('k8s-tools') {
                    echo "Restarting Deployment to pull new image..."
                    // This command triggers the rolling update
                    sh "kubectl rollout restart deployment/${IMAGE_NAME} -n weather-alert"
                    // Optional: Wait for it to be ready
                    sh "kubectl rollout status deployment/${IMAGE_NAME} -n weather-alert"
                }
            }
        }
    }
}