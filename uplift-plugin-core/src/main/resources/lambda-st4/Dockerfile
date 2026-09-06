FROM --platform=linux/⎨arch⎬ ubuntu:24.04 AS ⎨buildsite⎬
VOLUME [ "/out" ]
RUN apt-get update
RUN apt-get install -y build-essential gcc zlib1g-dev libz-dev libc6
RUN mkdir /graalvm
⎨if(disturi)⎬
ADD ⎨disturi⎬ /graal.tar.gz
WORKDIR /graalvm
RUN tar xzf /graal.tar.gz
WORKDIR /
RUN rm /graal.tar.gz
⎨else⎬
ADD ⎨distfile⎬ /graalvm
⎨endif⎬
RUN chmod 777 /graalvm/*/bin/*

FROM ⎨buildsite⎬ AS ⎨target⎬
CMD /graalvm/*/bin/native-image ⎨main⎬ \
 --verbose \
 --no-fallback \
 -g \
 -H:+UnlockExperimentalVMOptions \
 -H:+ReportExceptionStackTraces \
 --enable-url-protocols=https \
 ⎨enablepreview⎬ \
 ⎨addmodules⎬ \
 ⎨otheroptions⎬ \
 --target=linux-⎨arch⎬ \
 --class-path ⎨classpath⎬ \
 -o /out/⎨target⎬
