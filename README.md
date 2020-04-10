
![Android CI](https://github.com/deftmove/android_carpooling/workflows/Android%20CI/badge.svg)

# Description
DeftMove Carpooling is the first open source carpooling App that designed to be used by companies. The App can be fully customised by only changing the configuration file.
For more info please contact us at [dev@deftmove.com](mailto:dev@deftmove.com)

# Screenshots:
<TODO add screenshots here>


# Setup
1) Setup Firebase project
2) Generate a customer ID. To receive a new customer Id please contact us at [dev@deftmove.com](mailto:dev@deftmove.com)
3) Configure and apply your changes in Config file in ``/android_carpooling/builder_config/builder_config.json`


# Unit Tests
For running all tests run following command
```
./gradlew testDebugUnitTest

```

# Debug and development tools
When you run the project in debug mode, It contains some extra helps for developers to make fast login and create fake matches


# What should you know when you are adding a new module:
1) Gradle file of each module should be matched with other modules build.gradle
2) gitignore files are also common between modules. You can copy it from other modules
3) To be able to run Mockito you need to copy `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` to your module

# What should you know when adding a new plugin:
1) Plugins shall be inside `plugins` folder in the root folder
2) Each plugin shall have its own Registerer in its root package.
3) Registerer in your plugin will take care of adding you module into the project, you dont need to add anything into `settings.gradle`


# Rules for contributors:
-) Do not inject navigator by koin. Always pass it to the functions if needed
-) Use  MaterialButton instead of Button

# Report an issue
In case that you find an issue in the APP or want to request a new feature please create and issue [here](https://github.com/deftmove/android_carpooling/issues)

# Contribution
- Translate the App into your local language
- Make a plugin for the App and open source it for everyone to use
- Make a premium plugin for the App and contact us to add to our wizard panel
- Report an issue or help us fix an issue inside the App
