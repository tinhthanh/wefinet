FROM ubuntu:18.04
RUN apt-get update
RUN apt-get install -y openjdk-8-jre-headless
RUN apt-get install -y firefox

EXPOSE 9191

ENV JAVA_OPTS=""

ADD ./target/zalo-bot-1.0.0.jar app.jar

RUN sh -c 'touch /app.jar'

CMD [ "java", "-jar" ,"app.jar" ]
