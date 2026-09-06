# Hello, web!

This is a simple HTTP function will that reply with a friendly hello as a JSON str. It is intended to demonstrate usage of [uplift](../../).

## One-time setup

We need some groundwork to get started. 

* A running Docker service
* Access key/secret for an IAM user with sufficient permissions in AWS
* Maven properties to select AWS account and region, and to point to the access key/secrets

We will need docker to build, and a working IAM user in AWS with sufficient permissions to deploy. Maven also needs some properties to know about the IAM user.

### 1. Run Docker

Checklist for Docker:

* A docker daemon should be running and authenticated so it can pull [temurin](https://hub.docker.com/_/eclipse-temurin/) and [ubuntu](https://hub.docker.com/_/ubuntu) images from the [Docker hub](https://hub.docker.com/).
* The `docker` command line tool should be on `PATH` so that the build can execute it.

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

### 3. Configure the build

Copy [the template](./.mvn/maven.config.template) to `.mvn/maven.config` and fill it in. It points to the profile, and gives your account number and the region you want:

```
-Duplift.account=123456768910
-Duplift.region=us-east-1
-Duplift.profile=<profile with access keys to authorized role>
```

If any of the three is missing, the build stops during `validate` and says which.

## Build and run

### 1. Build uplift

The libraries aren't published anywhere yet. So, you need to publish 
[uplift](../..) to your local repo before running:

```bash
mvn -f ../../pom.xml install
```

This has to happen first, and not just the once. Maven resolves a build plugin before it
builds anything, so `uplift-maven-plugin` must already be in your local repository before
this example can start. Rerun it whenever the plugin changes.

### 2. Launch!

Then, you should be able to:

```bash
mvn uplift:deploy
```

And find your lambda running in the cloud eventually!

To find the URL of your service:

```bash
mvn uplift:ping
```

### 3. Look before you leap

To see the CloudFormation template that would be deployed, without touching AWS at all:

```bash
mvn uplift:init uplift:synth
```

The result lands in `hello-web-uplift/target/cdk-app/cdk.out/`.

To take it down again:

```bash
mvn uplift:destroy
```
