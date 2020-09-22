FROM registry-vpc.cn-hangzhou.aliyuncs.com/hbhb/8-jdk-alpine as builder
WORKDIR application
ADD ./target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM registry-vpc.cn-hangzhou.aliyuncs.com/hbhb/8-jdk-alpine
# kaptcha包导致的字体问题，解决方案-每次创建alpine容器时重新下载字体。故将其封装成自己的镜像。
#RUN apk add --update ttf-dejavu fontconfig && rm -rf /var/cache/apk/*
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"]
EXPOSE 8800