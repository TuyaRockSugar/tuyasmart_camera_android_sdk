# TuyaSmartCamera Android SDK

[中文版](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README-zh.md) | [English](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README.md)

------

## Features Overview

Tuya Smart Camera SDK provides the interface package for the communication with remote camera device to accelerate the application development process, including the following features:

- Preview the picture taken by the camera
- Play back recorded video of the remote camera
- Record video
- Talk to the remote camera

## Rapid Integration

 **Using AndroidStudio integration(Version 3.1.3 or above is supported)**

- Add Maven address to the root directory build.gradle:
```java
allprojects {
    repositories {
        ...
        maven {
            url 'https://raw.githubusercontent.com/TuyaInc/mavenrepo/master/releases'
        }
    }
}
```
- Add the following dependencies to the module build. gradle:

```java
dependencies {
    ...
    implementation "com.tuya.smart:tuyaCamera:3.0.5"
    implementation 'com.tuya.smart:tuyasmart:2.9.3'
}
```
For the instructions of AndroidStudio, please refer to: [AndroidStudio Guides](https://developer.android.com/studio/)



## Doc

Refer to Details: [Tuya Smart Camera Android SDK Doc](https://tuyainc.github.io/tuyasmart_camera_android_sdk_doc/en/)
