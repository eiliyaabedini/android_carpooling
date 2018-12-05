Releasing
========

 1. Set `bintray.user= <USERNAME HERE>` and `bintray.apikey= <API KEY HERE>` in `local.properties`
 2. Change/Increase the version in `build.gradle`.
 3. Set `USE_REMOTE_HEART` to false in `build.gradle`.
 4. Build project with `./gradlew clean build install`
 5. Upload libs to bintray with `./gradlew bintrayUpload`
 6. Set `USE_REMOTE_HEART` to true in `build.gradle`.
 7. Build and run the project