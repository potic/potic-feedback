FROM openjdk:8

RUN mkdir -p /usr/src/potic-feedback && mkdir -p /usr/app

COPY build/distributions/* /usr/src/potic-feedback/

RUN unzip /usr/src/potic-feedback/potic-feedback-*.zip -d /usr/app/ && ln -s /usr/app/potic-feedback-* /usr/app/potic-feedback

WORKDIR /usr/app/potic-feedback

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-feedback --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
