FROM eclipse-temurin:17-jre-jammy AS run
COPY /home/runner/work/monitoring/monitoring/build/libs/bmonitord.jar /bin/bmonitord.jar
#App has to run as root for ping check provider to work properly
WORKDIR /home/root
VOLUME /home/root
ENTRYPOINT exec java -jar /bin/bmonitord.jar
