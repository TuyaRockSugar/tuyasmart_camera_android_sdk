# TuyaSmartCamera Android SDK

[中文版](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README-zh.md) | [English](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README.md)

------

## Features Overview

Tuya Smart Camera SDK provides the interface package for the communication with remote camera device to accelerate the application development process, including the following features:

- Preview the picture taken by the camera
- Play back recorded video of the remote camera
- Record video
- Talk to the remote camera
- Add Cloud storage module

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
        maven { url 'https://jitpack.io' }
    }
}
```
add the following line to your module build.gradle:

```gradle
dependencies {
    ...
    // tuya camera module
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.14.3r133'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.14.4r134'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.13.0r128'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-message:3.13.0r128'
    implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.14.3r133'

    //not required Compatible with older versions
    implementation "com.tuya.smart:tuyaCamera:3.11.0r119h2"

    implementation 'com.tuya.smart:tuyasmart:3.12.4'
    
    //hybrid
    implementation 'com.tuya.smart:tuyasmart-webcontainer:3.12.6r125'
    implementation 'com.tuya.smart:tuyasmart-xplatformmanager:1.1.0'
    implementation "com.tuya.smart:tuyasmart-base:3.13.0r127"
    implementation 'com.tuya.smart:tuyasmart-appshell:3.10.0'
    implementation "com.tuya.smart:tuyasmart-stencilwrapper:3.13.0r127"
    implementation "com.tuya.smart:tuyasmart-framework:3.13.0r127-open-rc.1"
    implementation 'com.tuya.smart:tuyasmart-uispecs:0.0.3'
}
```
For the instructions of AndroidStudio, please refer to: [AndroidStudio Guides](https://developer.android.com/studio/)



## Doc

Refer to Details: [Tuya Smart Camera Android SDK Doc](https://tuyainc.github.io/tuyasmart_camera_android_sdk_doc/en/)

## Update log
- 2019.11.15
  - Update SDK (base 3.13.0r129)，ffmpeg 4.1.4
  - Update SDK Demo
- 2019.10.8
  - Update SDK（ base 3.12.6r125）
- 2019.8.23
  - Support P2P 3.0
- 2019.8.1
  - Add cloud storage module
- 2019.7.13
  - New SDK code API have changed.
  - To be compatible with the old version of sdk, use tuyaCamera: 3.11.0r119h2.
  - Suggestions for old API to upgrade New API

- 2019.6.11
  - Support arm64
