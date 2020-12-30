
FROM openjdk:8-jdk-alpine
RUN apk update && \
apk add --no-cache bash openrc openssh-server postgresql-client && \
rc-update add sshd
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh
ENTRYPOINT /entrypoint.sh
EXPOSE 22/tcp 8080/tcp
