# Hello, web!

This is a simple HTTP function will that reply with a friendly hello as a JSON string. It is intended to demonstrate usage of [uplift](../../).

## One-time setup

We need some groundwork to get started. 

* A running Docker service
* Access key/secret for an IAM user with sufficient permissions in AWS
* Gradle properties to select AWS account and region, and to point to the access key/secrets

We will need docker to build, a working IAM user in AWS with sufficient permissions to deploy. Gradle also needs some properties to know about the IAM user. 

### 1. Run Docker

Checklist for Docker:

* A docker daemon should be running and authenticated so it can pull [temurin](https://hub.docker.com/_/eclipse-temurin/) and [ubuntu](https://hub.docker.com/_/ubuntu) images from the [Docker hub](https://hub.docker.com/).
* The `docker` command line tool should be on `PATH` so that the Gradle process can execute it.

### 2. Identify in AWS

You need an AWS account and an IAM role with the permissions required for deployment. Creating one with [AllPermissions](https://us-east-1.console.aws.amazon.com/iam/home?region=us-east-1#/policies/arn:aws:iam::aws:policy/AdministratorAccess) will get you started.

We assume the file `~/.aws/credentials` contains the access key/secret for this user, in the usual way:

```
... other profiles ...

[myupliftingprofile]
aws_access_key_id = < AKIA... >
aws_secret_access_key = < 123... >

... other other profiles ...
```

### 3. Configure Gradle build

Add a `gradle.properties` file (based on e.g. [this one](./gradle.properties.template.txt)) file to point to the profile, along with your account number, and the desired region:

```
# Convert this to a gradle.properties file
account=123456768910
region=us-east-1
profile=<profile with access keys to authorized role> 
```

## Build and run

### 1. Build uplift

The libraries aren't published anywhere yet. So, you need to publish 
[uplift](../..) to your local repo before running:

```bash
cd ../.. && ./gradlew \
 build \
 publishToMavenLocal \
 publishPluginMavenPublicationToMavenLocal \
 ; cd -
```

### 2. Launch!

Then, you should be able to:

```bash
./gradlew uplift
```

And find your lambda running in the cloud eventually!

To find the URL of your service:

```bash
./gradlew uplift-ping
```
