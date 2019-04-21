# SDKMAN mongodb persistence

This project is a library used for persistence to MongoDB by all SDKMAN backend services.

This library is used in the following SDKMAN services:

* sdkman-website
* sdkman-candidates
* sdkman-hooks
* vendor-release

### Set Bintray credentials in sbt

    $ ./sbt bintrayChangeCredentials

### Run tests

    $ ./sbt test

### Publish to Bintray

    $ ./sbt "release cross with-defaults"
