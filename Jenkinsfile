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
                // bat 'mvn clean install' Con install se ejecuta todo el ciclo de vida: compile, test, package,
                //Ahora compila y empaqueta (genera el .jar), pero no hace los test.
                bat 'mvn clean package -DskipTests'
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

    post {
        success{
            emailext {
             subject: "Build Exitosa: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             body: "La build ha sido exitosa. Ver detalles en ${env.BUILD_URL}",
             to: 'jfrecios@gmail.com'
}
        }
        failure {
            emailext (
                subject: "Fallo en el pipeline: ${env.JOB_NAME}",
                body: "El job ${env.JOB_NAME} ha fallado en la etapa ${env.STAGE_NAME}.",
                to: 'jfrecios@mailtrap.io'
            )
        }
    }

}