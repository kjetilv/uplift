# uplift ☁️

Native images, to the cloud!

## What's all this then

This is my own toolkit for serverless development. I have split it out from a hobby project to make it more stand-alone.

It is designed to speed up development of small-ish Java-based applications to AWS Lambda, as native images.

If it also helps others – good. User feedback would be even better, but user support will be intermittent at best.

### Runtime libraries

* `uplift-kernel` contains various utilities
* `uplift-flogs` contains a faked implementations of the dreaded slfj4 and acl loggers, which will forward to Java logging. No other logging libraries needed,
  and no dynamic
  classloading needed to get started. The application can use the familiar slf4j API.
* `uplift-json` contains a minimal JSON parser, which can read and write `Map` and `List` structures. No Jakcson, but no startup time here either.
* `uplift-s3` contains code to talk to S3. No need for AWS libraries, for most use cases.
* `uplift-lambda` handles setup of the infrastructure between AWS Lambda and your application, providing a simple functional hook which receives a lambda
  payload, and returns a response. No AWS libraries in use here, either.

None of these are required, but I have found a use for them. Personal learning opportunities have trumped conventional choices in certain places.

### Testing libraries

* `uplift-flambda` contains a faked lambda service, which can be used for testing.
* `uplift-asynchttp` contains a *cough* async http server, because the world (and `uplift-flambda`) needed one.

Using these libraries is optional, but they can help keep time and space
constraints. In particular, time taken to boot a Java-based runtime.

### Plugins

Gradle plugins:

* `lambda-native` builds a native binary in zip form, which is what AWS wants to deploy. It requires a running docker daemon, and then sets up images needed to
  build an `aarch64` or `amd64` type binary for Linux.
* `uplift` deploys your app to Lambda, given an AWS account id and a region. Also:
    * the directory containing your credentials (by default the one in `~/.aws` / `$HOME`)
    * optionally a profile in this file (by defalt `default`
    * a running docker daemon

The plugins will handle docker moves required to download and build native images with graal, and to deploy to AWS with `aws-cdk`.

### Examples

Some example apps:

* `hello-world` will simply print hello world to stdout, meaning you can trigger it from a browser and find proof of life in the logs
* `hello-web` will actually respond to an HTTP request from the browser

## What's all that then (Roadmap)

Ideas? New ...

### Libraries

* `uplift-cdk` should provide some more help with setting up the stack

### Examples

* `hello-base64` will do something useful and return a base64 version of its input. It will also demonstrate unit testing with `uplift-flambda`
