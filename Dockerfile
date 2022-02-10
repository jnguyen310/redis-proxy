FROM maven:3.6.3-jdk-11 as builder
WORKDIR /usr/home/app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

ENTRYPOINT ["/bin/sh", "-c" , "java -jar /usr/home/app/target/rescale-redis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar &> /dev/null & mvn verify"]


