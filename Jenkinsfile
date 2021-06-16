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
               sh "pid=\$(lsof -i:8081 -t); kill -TERM \$pid "
                                 + "|| kill -KILL \$pid"
               withEnv(['JENKINS_NODE_COOKIE=dontkill']) {
                   sh 'nohup ./mvnw spring-boot:run -Dserver.port=8081 &'
               }
            }
        }
    }
}
