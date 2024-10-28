pipeline {
  agent {
    label 'prodsec'
  }

  tools {
    nodejs '8.16.0'
  }

  environment {
    NPM_TOKEN = credentials('auth0npm-npm-token')
  }

  options {
    timeout(time: 10, unit: 'MINUTES')
  }

  stages {
    stage('Build') {
      steps {
        sh 'npm install'
      }
    }

    stage('Test') {
      steps {
        sh 'npm run test'
      }
    }

    stage('Deploy') {
      steps {
        sh "echo //registry.npmjs.org/:_authToken=${env.NPM_TOKEN} > .npmrc"
        sh "npm publish"
      }
    }
  }

  post {
    always{
      deleteDir()
    }
  }
}
