#!/bin/bash

echo "entered bootstrap.sh"
sed -i s/##SERVER##/$(echo $DB_PORT_5432_TCP_ADDR)/g /application.yml
sed -i s/##PORT##/$(echo $DB_PORT_5432_TCP_PORT)/g /application.yml
sed -i s/##PASSWORD##/$(echo $DB_POSTGRES_PASSWORD)/g /application.yml

echo "starting the spring boot application"
eval java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar  $APP_PROPERTIES
