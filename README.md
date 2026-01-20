# uplift ☁️

Native images, to the cloud!

To try it out, start by installing to local repo:

```bash
./gradlew publishToMavenLocal publishPluginMavenPublicationToMavenLocal
```

Then check out [an example](./examples/hello-web) that deploys a native service.

## What's all this then

Homegrown toolkit for serverless development. Designed to speed up development of small-ish Java-based applications to
AWS Lambda, running as native images.

### Runtime libraries

These can be used by lambdas you want to deploy, but they are not required:

* [`uplift-util`](uplift-util) contains various utilities
* [`uplift-hash`](uplift-hash) hashes stuff
* [`uplift-flogs`](uplift-flogs) contains mininmal, faked implementations of the dreaded slfj4 and apache commons logger interfaces.
  These will forward to the Java logging library. No other logging
  libraries needed,
* [`uplift-kernel`](./uplift-kernel) contains various utilities
* [`uplift-edam`](uplift-edam) logs exceptions, sparingly
* [`uplift-edamame`](uplift-edamame) creates canonical trees
* [`uplift-json`](./uplift-json) contains a minimal JSON parser. Why not, right? It can read and write `Map` and `List`
* [`uplift-json-mame`](./uplift-json-mame) reads json trees with edamame
* [`uplift-json-gen`](./uplift-json-gen) generates JSON r/w code from annotated records, now we can read and write
  records
* [`uplift-json-anno`](./uplift-json-anno) the annotations for the above
* [`uplift-s3`](./uplift-s3) home-made, artisan code to talk to S3. No need for AWS libraries, for most use
  cases.
* [`uplift-lambda`](./uplift-lambda) handles setup of the infrastructure between AWS Lambda and your application,
  providing a simple functional hook which receives a lambda
  payload, and returns a response. No AWS libraries in use here, either.

Using these libraries is optional, but they can help keep time and space constraints. In particular, the time
taken to start up a Java-based runtime.

### Testing libraries

* [`uplift-flambda`](./uplift-flambda) contains a faked lambda service, which can be used for testing.
* [`uplift-asynchttp`](./uplift-asynchttp) contains a *cough* async http httpHandler, because the world needed one more of
  those.

None of these are required, but I use them. Personal learning opportunities may have trumped conventional choices in
some of the more interesting places.

### Plugins

Gradle plugins:

* [`lambda-native`](uplift-gradle-plugins) builds a native binary in zip form, which is what AWS wants to deploy. It
  sets up images needed to build an `aarch64` or `amd64` type binary for Linux.
* [`uplift`](./uplift-gradle-plugins) deploys your app to Lambda, given AWS credentials, a region and
    * the directory containing your credentials (by default the one in `~/.aws` / `$HOME`)
    * optionally a profile in this file (by defalt `default`)
    * a running docker daemon

The plugins will handle docker moves required to download and build native images with graal, and to deploy to AWS with
`aws-cdk`.

### Examples

Some example apps:

* [`hello-web`](examples/hello-web) is a simple, web-based hello world.
    * Responds to a GET HTTP request from the browser, with a friendly hello.
    * Uses [`uplift-flambda`](./uplift-flambda) to test the lambda function.

## What's all that then (Roadmap)

Possible upcoming stuff:

### Libraries

* `uplift-cdk` should provide some more help with setting up the stack.
