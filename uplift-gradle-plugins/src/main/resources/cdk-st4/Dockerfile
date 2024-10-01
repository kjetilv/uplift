FROM ⎨arch⎬/maven:3.9.6-eclipse-temurin-23 as cdk-site
RUN apt-get update
RUN apt-get -y install npm ca-certificates && update-ca-certificates
VOLUME [ "/root/.aws", "/opt/app", "/lambdas" ]
WORKDIR /opt/app
RUN npm install -g node@18 aws-cdk
