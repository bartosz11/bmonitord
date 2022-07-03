FROM eclipse-temurin:17-jre
WORKDIR /home/container
COPY build/libs/monitoring.jar monitoring.jar
ENTRYPOINT [ "java", "-jar", "monitoring.jar" ]