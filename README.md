
For testing Deeplinks you can run following command:
```
adb shell am start -W -a android.intent.action.VIEW -d "linkHere" com.deftmove
```


For running all tests run following command
```
./gradlew testDebugUnitTest

```


For Running style check run following command
```
./gradlew detekt
```

for Espresso tests check this link for extra configurations:
https://developer.android.com/studio/test



# TODO after making a new module:
Copy gradle file from other modules
Copy gitignore from other  modules
Copy src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker from other modules

Attention:
-) Do not inject navigator by koin. Always pass it to the functions if needed


Views:
We use  MaterialButton instead of Button


TODO:
- [ ] Update Readme
- [ ] Move to github
