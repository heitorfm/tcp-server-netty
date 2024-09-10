FROM ghcr.io/graalvm/jdk:ol9-java17-22.3.2 AS application

# app info
ENV APP_NAME=tcpserver
ENV APP_VERSION=1.0.0
ENV APP_DIR=/home/$APP_NAME

# setup app folder
RUN mkdir -p $APP_DIR

#setup user
ARG UID=10001
RUN groupadd --gid ${UID} appuser \
    && adduser --gid ${UID} --shell /bin/false --uid ${UID} appuser \
    && chown -R appuser:appuser $APP_DIR

USER appuser

# machine timezone configuration
ENV TZ="America/Sao_Paulo"

EXPOSE 10000

WORKDIR $APP_DIR

# Application setup
ENV LOG4J2_FILE=${APP_DIR}/log4j2.xml
COPY src/main/resources/log4j2.xml $LOG4J2_FILE
COPY ${JAR_FOLDER}${JARNAME} $APP_DIR
CMD ["sh", "-c", "java -Dlog4j.configurationFile=${LOG4J2_FILE} -Djava.security.egd=file:/dev/urandom -jar /home/tcpserver/${JARNAME}"]