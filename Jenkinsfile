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
               sh 'echo JAVA_HOME is $JAVA_HOME'
               sh 'echo PATH is $PATH'
               sh 'ENV JAVA_HOME=/usr/local/src/jdk1.8.0_281'
               sh 'ENV CLASS_PATH=$JAVA_HOME/lib'
               sh 'ENV PATH=$PATH:$JAVA_HOME/bin'
               sh 'export PATH JAVA_HOME CLASSPATH'
               sh 'echo JAVA_HOME is $JAVA_HOME'
               sh 'echo PATH is $PATH'
               withEnv(['JENKINS_NODE_COOKIE=background_job']) {
               sh '"nohup java -jar target/elasticsearch-0.0.1-SNAPSHOT.jar >/dev/null 2>&1 &"'
               }
            }
        }
    }
}
