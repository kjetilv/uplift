FROM --platform=linux/⎨arch⎬ ubuntu:24.04 AS ⎨buildsite⎬
VOLUME [ "/out" ]
RUN apt-get update
RUN apt-get install -y build-essential gcc zlib1g-dev libz-dev libc6
RUN mkdir /graalvm
ADD ⎨dist⎬
RUN chmod 777 /graalvm/*/bin/*

FROM ⎨buildsite⎬ AS ⎨target⎬
CMD /graalvm/*/bin/native-image ⎨main⎬ \
 --verbose \
 --no-fallback \
 -g \
 -H:+UnlockExperimentalVMOptions \
 -H:+ReportExceptionStackTraces \
 --enable-url-protocols=https \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.DefaultLogEntryFormatter \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.Flogs\$Settings \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.Flogger \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.FloggerFinder \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.FloggerFinder\$SystemFlogger \
 --initialize-at-build-time=com.github.kjetilv.uplift.flogs.LogLevel \
 ⎨enablepreview⎬ \
 ⎨addmodules⎬ \
 ⎨otheroptions⎬ \
 --target=linux-⎨arch⎬ \
 --class-path ⎨classpath⎬ \
 -o \
 /out/⎨target⎬
