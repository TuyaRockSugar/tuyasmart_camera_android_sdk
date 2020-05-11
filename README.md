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
  buildscript {

     repositories {
         ...
         maven {
             url 'https://maven-other.tuya.com/repository/maven-releases/'
         }
         maven {
             url 'https://maven-other.tuya.com/repository/maven-snapshots/'
         }
     }
     dependencies {
         classpath 'com.android.tools.build:gradle:3.1.4'
         classpath 'com.tuya.android.module:tymodule-config:0.5.1'
         // NOTE: Do not place your application dependencies here; they belong
         // in the individual module build.gradle files
     }
 }

 allprojects {
     repositories {
         ...
         maven {
             url 'https://maven-other.tuya.com/repository/maven-releases/'
         }
         maven {
             url 'https://maven-other.tuya.com/repository/maven-snapshots/'
         }
     }
 }
```
add the following line to your module build.gradle:

```gradle
apply plugin: 'tymodule-config'

defaultConfig {
     ndk {
         abiFilters "armeabi-v7a","arm64-v8a"
     }
  }
     dependencies {
         implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
         implementation 'com.alibaba:fastjson:1.1.67.android'
         implementation 'com.squareup.okhttp3:okhttp-urlconnection:3.12.3'
         // implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0'

         // required tuya home sdk
         implementation 'com.tuya.smart:tuyasmart:3.17.0-beta1'

         // tuya camera module
         implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.14.3r133'
         implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.17.0r139'
         implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.13.0r129h1'
         implementation 'com.tuya.smart:tuyasmart-ipc-camera-message:3.13.0r128'
         implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.17.0r139'
          //messge center imagepipeline 
         implementation 'com.tuya.smart:tuyasmart-imagepipeline-okhttp3:0.0.1'
         implementation 'com.facebook.fresco:fresco:1.3.0'

     }

 repositories {
     mavenLocal()
     jcenter()
     google()
 }
```
For the instructions of AndroidStudio, please refer to: [AndroidStudio Guides](https://developer.android.com/studio/)



## Doc

Refer to Details: [Tuya Smart Camera Android SDK Doc](https://tuyainc.github.io/tuyasmart_camera_android_sdk_doc/en/)

## Update log
- 2020.5.9
  - Update SDK(base 3.17.0r139),fix audio problems (switch definition sound off) and .so crash, improve stability;
  - Add reporting callback of all DP point operations of the device
  - Update Demo
- 2020.3.31
  - Update SDK，modify messageCenter Video player bug on armabi .so
- 2020.3.4
  - Update SDK(base 3.15.0r135),add message center cloud strage video
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
