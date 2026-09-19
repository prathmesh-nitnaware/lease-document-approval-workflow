pipeline {
  agent any
  parameters {
    string(name: 'APP_PORT', defaultValue: '8081', description: 'Port the app will listen on')
    choice(name: 'SPRING_PROFILE', choices: ['dev', 'staging'], description: 'Active Spring profile')
  }
  environment {
    DB_PASSWORD = credentials('mysql-db-password')
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build') {
      steps {
        bat '''
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
    stage('Deploy') {
      steps {
        bat '''
          REM Stop any previously running instance on this port
          for /f "tokens=5" %%P in ('netstat -aon ^| findstr :%APP_PORT% ^| findstr LISTENING') do taskkill /PID %%P /F

          REM Prevent Jenkins from killing this process when the build step ends
          set BUILD_ID=dontKillMe

          start "lease-workflow-app" javaw -jar target\\lease-document-approval-workflow-0.0.1-SNAPSHOT.jar --server.port=%APP_PORT% --spring.profiles.active=%SPRING_PROFILE% --spring.datasource.password=%DB_PASSWORD%
        '''
        script {
          sleep(time: 15, unit: 'SECONDS')
        }
        bat 'curl -f http://localhost:%APP_PORT%/actuator/health || curl -f http://localhost:%APP_PORT%/'
      }
    }
  }
  post {
    success {
      echo "Deployed at http://localhost:${params.APP_PORT} (profile: ${params.SPRING_PROFILE})"
    }
    failure {
      echo "Deploy failed — check console log"
    }
  }
}
