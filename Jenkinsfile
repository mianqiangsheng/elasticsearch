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
//                sh 'java -jar /root/elasticsearch-0.0.1-SNAPSHOT.jar'
               sh 'cp /root/jenkins/workspace/test_dev/target/elasticsearch-0.0.1-SNAPSHOT.jar /root/elasticsearch-0.0.1-SNAPSHOT.jar'
//                withEnv(['JENKINS_NODE_COOKIE=background_job']) {
               sh 'nohup java -jar /root/elasticsearch-0.0.1-SNAPSHOT.jar >/dev/null 2>&1 &'
//                }
            }
        }
    }
}
