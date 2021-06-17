FROM openjdk:8u292-jdk
WORKDIR /application
ADD elasticsearch-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["nohup","java","-jar","app.jar",">/dev/null","2>&1","&"]

EXPOSE 8081
