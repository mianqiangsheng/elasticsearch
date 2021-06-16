pipeline {
    agent {
        docker {
            image 'maven:3.6.3-jdk-8'
            label 'slave_node'
            args '-v /root/.m2:/root/.m2'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
//         stage('Build') {
//             steps {
//                 sh 'mvn -B clean package'
//             }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
//         stage('Deliver') {
//             steps {
//                 sh 'chmod 745 ./deliver.sh'
//                 sh './deliver.sh'
//             }
//         }
        stage('Run') {
            steps {
                sh 'nohup java -jar target/elasticsearch-0.0.1-SNAPSHOT.jar >/dev/null 2>&1 &'
            }
        }
    }
}
