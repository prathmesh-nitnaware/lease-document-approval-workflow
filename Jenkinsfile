pipeline {
  agent any
  parameters {
    string(name: 'BRANCH_NAME', defaultValue: 'develop', description: 'Branch to build and deploy')
    string(name: 'APP_PORT', defaultValue: '8081', description: 'Port the app will listen on')
    choice(name: 'SPRING_PROFILE', choices: ['dev', 'staging'], description: 'Active Spring profile')
  }
  environment {
    DB_PASSWORD = credentials('mysql-db-password')
  }
  stages {
    stage('Checkout') {
      steps {
        git branch: params.BRANCH_NAME,
            credentialsId: 'github-pat',
            url: 'https://github.com/prathmesh-nitnaware/lease-document-approval-workflow'
      }
    }
    stage('Build') {
      steps {
        bat '''
          for /f "tokens=5" %%P in ('netstat -aon ^| findstr :%APP_PORT% ^| findstr LISTENING') do taskkill /PID %%P /F || rem
          set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lease_workflow
          set SPRING_DATASOURCE_USERNAME=root
          set SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD%
          mvn clean package
        '''
      }
    }
    stage('Package') {
      steps {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }
    stage('Docker Build') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
          script {
            env.DOCKERHUB_LOGIN_USER = env.DOCKERHUB_USER.split('/')[0].trim()
            env.GIT_SHA = bat(script: '@git rev-parse --short HEAD', returnStdout: true).trim()
            env.IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_SHA}"
            env.DOCKER_IMAGE = "${env.DOCKERHUB_LOGIN_USER}/lease-workflow-app"
          }
          bat 'docker build -t %DOCKER_IMAGE%:%IMAGE_TAG% -t %DOCKER_IMAGE%:latest .'
        }
      }
    }
    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
          script {
            env.DOCKERHUB_LOGIN_USER = env.DOCKERHUB_USER.split('/')[0].trim()
          }
          bat 'echo %DOCKERHUB_PASS% | docker login -u %DOCKERHUB_LOGIN_USER% --password-stdin'
          bat 'docker push %DOCKER_IMAGE%:%IMAGE_TAG%'
          bat 'docker push %DOCKER_IMAGE%:latest'
          bat 'docker logout'
        }
      }
    }
    stage('Deploy') {
      steps {
        bat '''
          docker stop lease-workflow-container || exit 0
          docker rm lease-workflow-container || exit 0
          docker run -d --name lease-workflow-container -p %APP_PORT%:%APP_PORT% ^
            -e SERVER_PORT=%APP_PORT% ^
            -e SPRING_PROFILES_ACTIVE=%SPRING_PROFILE% ^
            -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/lease_workflow ^
            -e SPRING_DATASOURCE_USERNAME=root ^
            -e SPRING_DATASOURCE_PASSWORD=%DB_PASSWORD% ^
            %DOCKER_IMAGE%:%IMAGE_TAG%
        '''
        script {
          sleep(time: 15, unit: 'SECONDS')
        }
        bat 'curl -f http://localhost:%APP_PORT%/actuator/health || curl -f http://localhost:%APP_PORT%/'
      }
    }
  }
  post {
    always {
      junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
    }
    success {
      echo "Deployed container at http://localhost:${params.APP_PORT} (profile: ${params.SPRING_PROFILE}, image: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG})"
    }
    failure {
      echo "Deploy failed — check console log"
    }
  }
}
