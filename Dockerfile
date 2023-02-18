FROM eclipse-temurin:17-jre-jammy AS run
COPY build/libs/bmonitord.jar /bin/bmonitord.jar
#https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#sort-multi-line-arguments
RUN apt update && apt install iputils-ping -y --no-install-recommends && rm -rf /var/lib/apt/lists/*
#App has to run as root for ping check provider to work properly
WORKDIR /home/root
VOLUME /home/root
ENTRYPOINT exec java -jar /bin/bmonitord.jar
