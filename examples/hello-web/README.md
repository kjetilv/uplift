# Hello, web!

This function will reply with a friendly hello as a JSON string.

## Steps

We will need docker to build, a working IAM user in AWS with sufficient permissions to deploy.Gradle also needs some properties to know about the IAM user. 

#### 1. Run Docker

Checklist for Docker:

* A docker daemon should be running and authenticated so it can pull [temurin](https://hub.docker.com/_/eclipse-temurin/) and [ubuntu](https://hub.docker.com/_/ubuntu) images from the [Docker hub](https://hub.docker.com/).
* The `docker` command line tool should be on `PATH` so that the gradle process can run it.

#### 2. Build uplift

The libraries aren't published anywhere yet. So, publish [uplift](../..) to local repo before running:

```
cd ../.. && ./gradlew build publishToMavenLocal && cd -
```

#### 3. Identify in AWS

You need an AWS account and an IAM role with the permissions required for deployment. Creating one with [AllPermissions](https://us-east-1.console.aws.amazon.com/iam/home?region=us-east-1#/policies/arn:aws:iam::aws:policy/AdministratorAccess) will get you started.

We assume the file `~/.aws/credentials` contains the access key+secret for this user, in the usual way:

```
... other profiles ...

[myupliftingprofile]
aws_access_key_id = < AKIA... >
aws_secret_access_key = < 123... >

...yet more profiles...
```

### 4. Configure Gradle build

Add a `gradle.properties` file (based on e.g. [this one](./gradle.properties.template.txt)) file to point to the profile, along with the account number and the desired region:

```
# Convert this to a gradle.properties file
account=123456768910
region=us-east-1
profile=<profile with access keys to authorized role> 
```

## 5. Launch!

Then, you should be able to:

```
./gradlew uplift
```

And find your lambda running in the cloud eventually!

To find the URL of your service:

```
./gradlew uplift-ping
```
