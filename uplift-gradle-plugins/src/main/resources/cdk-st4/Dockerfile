FROM --platform=linux/⎨arch⎬ eclipse-temurin:24
RUN apt-get update
RUN apt-get -y install npm maven ca-certificates && update-ca-certificates
VOLUME [ "/root/.aws", "/opt/app", "/lambdas" ]
WORKDIR /opt/app
RUN npm install -g node@22 aws-cdk -y
