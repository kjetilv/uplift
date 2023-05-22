# Hello, web!

This function will reply with a friendly hello as a JSON string.

## Steps

We will need docker to build, a working IAM user in AWS with sufficient permissions to deploy.Gradle also needs some properties to know about the IAM user. 

#### 1. Run Docker

Checklist for Docker:

* A docker daemon should be running and authenticated so it can pull [temurin](https://hub.docker.com/_/eclipse-temurin/) and [ubuntu](https://hub.docker.com/_/ubuntu) images from the [Docker hub](https://hub.docker.com/).
* The `docker` command line tool should be on `PATH` so that the gradle process can run it.

#### 2. Build uplift

The libraries aren't published anywhere yet. So, publish [uplift](../../) to local repo before running:

```
cd ../.. && ./gradlew build publishToMavenLocal && cd -
```

#### 3. Identify in AWS

You need an AWS account and an IAM user the permissions required to actually run in the cloud. For permissions that
work, [AllPermissions](https://us-east-1.console.aws.amazon.com/iam/home?region=us-east-1#/policies/arn:aws:iam::aws:policy/AdministratorAccess) is a good start
to get things rolling.

We assume the file `~/.aws/credentials` contains access key for this user in the usual way, under the name `myprofile`: (Or something else.)

```
... other profiles ...

[myprofile]
aws_access_key_id = AKIA...
aws_secret_access_key = ...

...yet more profiles...
```

### 4. Configure Gradle build

Add a `gradle.properties` file to point to the profile (whatever you called it) and your account number, along with the desired region:

```
account=123456781234
region=us-east-1
profile=myprofile
```

## 5. Launch!

Then, you should be able to:

```
./gradlew uplift
```

And find your lambda running in the cloud eventually!
