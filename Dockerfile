FROM node:18-bullseye-slim AS build_node
COPY frontend/ /home/root
WORKDIR /home/root
RUN npm i && npm run build
RUN gzip --keep --best -r dist/

FROM eclipse-temurin:17-alpine AS build_java
WORKDIR /home/root
COPY src/ src/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY gradle/ gradle/
COPY --from=build_node /home/root/dist src/main/resources/static
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine AS run
COPY --from=build_java /home/root/build/libs/monitoring.jar /bin/monitoring.jar
#App has to run as root for ping check provider to work properly
WORKDIR /home/root
VOLUME /home/root
ENTRYPOINT exec java -jar /bin/monitoring.jar
