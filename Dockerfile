FROM openjdk:8-jre

COPY [ "pkg/docker/bootstrap.sh", "application.yml", "/" ]
COPY build/libs/service-virtualnetwork*.jar /app.jar
RUN chmod +x /bootstrap.sh
ENTRYPOINT ["/bootstrap.sh"]
EXPOSE 46016