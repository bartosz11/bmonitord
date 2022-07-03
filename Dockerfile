FROM eclipse-temurin:17-jre-alpine
RUN adduser --home /home/container -D container
USER container
WORKDIR /home/container
COPY build/libs/monitoring.jar /bin/monitoring.jar
ENTRYPOINT [ "java", "-jar", "/bin/monitoring.jar" ]