# syntax=docker/dockerfile:1
FROM amazoncorretto:17-alpine
MAINTAINER Stefan Oltmann

WORKDIR /server

# The application
COPY mediathek-downloader-server.jar .

# REST interface
EXPOSE 60000

# Config files and logs go here
VOLUME /server/data

# Run the application
CMD ["java", "-jar", "/server/mediathek-downloader-server.jar"]
