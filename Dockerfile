FROM maven:3-openjdk-18 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:18-jdk
COPY --from=build /home/app/target/*.jar /usr/local/lib/kava-web.jar
COPY --from=build /home/app/target/lib/*.jar /usr/local/lib/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/kava-web.jar"]
