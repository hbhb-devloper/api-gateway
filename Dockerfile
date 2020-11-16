FROM openjdk:8-jdk-alpine as builder
WORKDIR application
ADD ./target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:8-jdk-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=10040", "org.springframework.boot.loader.JarLauncher"]
EXPOSE 8880
EXPOSE 10040