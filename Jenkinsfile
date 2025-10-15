pipeline {
    agent any

    stages {
       stage('Checkout') {
            steps {
                // From branch main, get some code from a GitHub repository
                git credentialsId: 'github-creds', branch: 'main', url: 'https://github.com/jfrecio0157/SistemaBiblioteca.git'
            }
       }
       stage('Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                  bat 'mvn test'
            }

            //Pasos de compilacion posteriores
            post {
            // If Maven was able to run the tests, even if some of the test
            // failed, record the test results and archive the jar file.
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}