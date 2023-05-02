# Hello, web!

This function will reply with a friendly hello as a JSON string.

Publish uplift to local repo before running:

`./gradlew publishToMavenLocal`

Then add a `gradle.properties` file to set your account, the desired region 
and the profile to use, e.g.:

```
account=123456781234
region=us-east-1
profile=myprofile
```

This assumes that the file `~/.aws/credentials` exists with the usual contents:

```
[myprofile]
aws_access_key_id = AKIA...
aws_secret_access_key = ...
```

The access key/secret should point to a user with sufficient permissions.  

[AllPermissions](https://us-east-1.console.aws.amazon.com/iam/home?region=us-east-1#/policies/arn:aws:iam::aws:policy/AdministratorAccess) is a good start!

Then, you should be able to:

```
./gradlew uplift
```

