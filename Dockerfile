FROM eclipse-temurin:17-jre-jammy AS run
COPY build/libs/bmonitord.jar /bin/bmonitord.jar
#https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#sort-multi-line-arguments
RUN apt update && apt install iputils-ping curl -y --no-install-recommends && rm -rf /var/lib/apt/lists/*
#App has to run as root for ping check provider to work properly
WORKDIR /home/root
VOLUME /home/root
#Consider switching your mail server if it causes a healthcheck with 15s timeout to fail /j
#bmonitord takes a while to start, so 120s start period
HEALTHCHECK --interval=45s --timeout=15s --start-period=120s \
  CMD curl --fail http://127.0.0.1:8080/app/health || exit 1
#default port
EXPOSE 8080
ENTRYPOINT exec java -jar /bin/bmonitord.jar
