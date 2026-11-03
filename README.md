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
* [`uplift.hash`](uplift.hash) hashes stuff
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

## Performance

A round of performance work on JSON parsing and tree canonicalization, measured with the JMH benchmarks in
[`uplift-json-jmh`](./uplift-json-jmh).

The "before" numbers were taken after the correctness fixes made in the same round, so the gains come from the
performance changes alone. Setup: Apple M4 Pro, 14 cores, Oracle GraalVM 25 (JVM mode), one fork, 3 to 5 measured
iterations. Treat differences under about 5% as noise.

### Results

| Benchmark                                                     | Before         | After          | Change |
|---------------------------------------------------------------|----------------|----------------|--------|
| `ReadBenchmark.readTweetUplift`: typed record, 2.7 KB tweet   | 120,600 ops/s  | 187,400 ops/s  | +55%   |
| allocation per op                                             | 12.4 KB        | 9.2 KB         | −26%   |
| `CacheBenchmark.readJsonUncached`: 76 MB JSON Lines into maps | 2.28 ops/s     | 4.11 ops/s     | +80%   |
| allocation per op                                             | 1.50 GB        | 1.09 GB        | −27%   |
| `CacheBenchmark.readJsonCached`: same file, `uplift-json-mame` | 0.78 ops/s     | 1.24 ops/s     | +59%   |
| allocation per op                                             | 4.86 GB        | 2.02 GB        | −58%   |
| `MemoizerBenchmark.memoize`: 20,000 maps into `MapsMemoizer`  | 5.35 ops/s     | 8.37 ops/s     | +56%   |
| allocation per op                                             | 859 MB         | 274 MB         | −68%   |

For comparison, Jackson does 195,900 ops/s on the tweet and 5.05 ops/s on the uncached file.

Memory retained after loading all 78,869 lines of the JSON Lines file (`MemoizerFootprint`):

| Form                                  | Before | After  |
|---------------------------------------|--------|--------|
| Parsed maps, not memoized             | 228 MB | 228 MB |
| Memoized, live view (`maps()`)        | 156 MB | 146 MB |
| Memoized, flat copy (`maps(true)`)    | 156 MB | 140 MB |

### What changed

[`uplift-json`](./uplift-json):

* The lexer scans a byte array in tight loops, instead of reading one byte at a time through a virtual call. Byte
  arrays, array-backed `ByteBuffer`s and heap `MemorySegment`s are scanned in place. Streams and channels fill an 8 KB
  buffer.
* Strings without escapes, and numbers, are read as views into the input, without copying. Each string is decoded
  once.
* Numbers without an exponent and with at most 18 digits are parsed directly, without `BigDecimal`.
* Repeated field names are reused from a small cache, so they do not allocate.
* The lexeme buffer no longer grows by catching an exception, and per-parse buffers are smaller and created on
  demand.

[`uplift-hash`](./uplift-hash):

* Hash builders hash each item directly, without a stream pipeline per item.
* The pool of `MessageDigest`s is a fixed array of slots, used without locks and without allocation. The pool is
  chosen by the type of the hash kind; the earlier map lookup computed a record's `hashCode` and `equals` on every
  hash.
* Hash builders are documented as not thread-safe; callers must use one builder per thread at a time.

[`uplift-edamame`](./uplift-edamame) and [`uplift-json-mame`](./uplift-json-mame):

* The canonicalizer looks up a hash before building anything. A known subtree is confirmed equal by comparing it
  directly, without allocating.
* Key caches look up with `get` before `computeIfAbsent`.
* Canonical maps are stored compactly (`Map.ofEntries`), except maps that hold a null value.
* `maps(true)` returns one flat map from identifier to stored map.
* `MapsMemoizer` reads take no lock.

Tried and dropped: a cache from leaf value to hash. It gained 6% when the same `String` objects were stored again, but
lost 6% on freshly parsed input, which is the normal case.

### Reproducing

```bash
mvn -pl uplift-json-jmh -am install -DskipTests
cd uplift-json-jmh
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
CP=target/classes:$(cat cp.txt)

# Throughput and allocation
java -cp "$CP" org.openjdk.jmh.Main 'ReadBenchmark|CacheBenchmark|MemoizerBenchmark' -f 1 -prof gc

# Retained memory; add the argument copy for maps(true)
java -Xmx4g -cp "$CP" com.github.kjetilv.uplift.jmh.MemoizerFootprint
```

`CacheBenchmark` must run from `target/classes`, as above: it reads its data file through a file path, which does not
work from the shaded JMH jar.

## What's all that then (Roadmap)

Possible upcoming stuff:

### Libraries

* `uplift-cdk` should provide some more help with setting up the stack.
