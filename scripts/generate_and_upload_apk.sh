#!/usr/bin/env bash

#https://github.com/gdrive-org/gdrive
#brew install gdrive
./gradlew assembleDebug

gdrive upload --parent 19FA2NsF1BpWB5ODzx4KL5RDWE_GG9LL- app/build/outputs/apk/debug/app-debug.apk