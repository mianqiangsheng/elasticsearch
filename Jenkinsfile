// pipeline 关闭已经启动的java服务，拷贝maven打的jar包并后台运行
pipeline {
    agent { node { label 'linux_1 && slave_node && docker' } }
    tools {
            maven 'linux_maven'
            jdk 'linux_jdk8'
        }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B clean package'
            }
        }
        stage('Dockerize') {
            environment {
                    REPOSITORY='myregistry.domain.com/elasticsearch:1.0.0'
                }
            steps {
               sh 'printenv'

               sh '''
                   docker rm -f elasticsearch
                   docker image rm -f $REPOSITORY
                   docker build -t $REPOSITORY ./
                   docker push $REPOSITORY
               '''
               withEnv(['JENKINS_NODE_COOKIE=background_job']) {
               sh 'docker run -dp 8081:8081 --name elasticsearch $REPOSITORY'
               }
            }
        }
    }
}
