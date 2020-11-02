# SDKMAN mongodb persistence

This project is a library used for persistence to MongoDB by all SDKMAN backend services.

This library is used in the following SDKMAN services:

* [sdkman-website](https://github.com/sdkman/sdkman-website)
* [sdkman-candidates](https://github.com/sdkman/sdkman-candidates)
* [sdkman-hooks](https://github.com/sdkman/sdkman-hooks)
* [vendor-release](https://github.com/sdkman/vendor-release)

### Set Bintray credentials in sbt

    $ ./sbt bintrayChangeCredentials

### Run tests

    $ ./sbt test

### Publish to Bintray

    $ ./sbt "release cross with-defaults"
