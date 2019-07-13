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

add the following line to your project build.gradle:

```gradle
  allprojects {
    repositories {
        ...
        maven {
            url 'https://raw.githubusercontent.com/TuyaInc/mavenrepo/master/releases'
        }
    }
}
<<<<<<< HEAD
```
add the following line to your module build.gradle:

```gradle
dependencies {
    ...
    // tuya camera module
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.11.0r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.11.0r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.11.0r119'

    implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.11.0r119'

    //not required Compatible with older versions
    implementation "com.tuya.smart:tuyaCamera:3.11.0r119h2"

    implementation 'com.tuya.smart:tuyasmart:3.9.6'
}
=======
>>>>>>> release/v3.11.0r119
```
add the following line to your module build.gradle:

```gradle
dependencies {
    ...
    // tuya camera module
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.11.0r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.11.0r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.11.0r119'

    implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.11.0r119'

    //not required Compatible with older versions
    implementation "com.tuya.smart:tuyaCamera:3.11.0r119h2"

    implementation 'com.tuya.smart:tuyasmart:3.9.6'
}
```
For the instructions of AndroidStudio, please refer to: [AndroidStudio Guides](https://developer.android.com/studio/)



## Doc

Refer to Details: [Tuya Smart Camera Android SDK Doc](https://tuyainc.github.io/tuyasmart_camera_android_sdk_doc/en/)

## Update log
- support arm64
