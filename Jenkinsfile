pipeline {
    agent none
    stages {
        stage('Example Build') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-8'
                    label 'slave_node'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                sh 'mvn -B clean package'
            }
        }
        stage('Example Run') {
            agent { node { label 'slave_node' } }
            steps {
               sh 'printenv'
               sh 'echo JAVA_HOME is $JAVA_HOME'
               sh 'echo PATH is $PATH'
               sh 'java -version'

               PROCESS_ID = sh (script: "ps -ef|grep elasticsearch-0.0.1-SNAPSHOT | awk '\$8 ~ /java/ {print \$2}'", returnStdout: true).trim()
               echo "PROCESS_ID=" + PROCESS_ID
               if (PROCESS_ID != "") {
                   sh 'echo Kill process: ${PROCESS_ID}'
                   sh 'kill -9 ${PROCESS_ID}'
               }

               sh 'cp /root/jenkins/workspace/test_dev/target/elasticsearch-0.0.1-SNAPSHOT.jar /root/jenkins/elasticsearch-0.0.1-SNAPSHOT.jar'
               withEnv(['JENKINS_NODE_COOKIE=background_job']) {
//                sh 'nohup java -jar /root/jenkins/workspace/test_dev/target/elasticsearch-0.0.1-SNAPSHOT.jar >/dev/null 2>&1 &'
               sh 'nohup java -jar /root/jenkins/elasticsearch-0.0.1-SNAPSHOT.jar >/dev/null 2>&1 &'
               }
            }
        }
    }
}
