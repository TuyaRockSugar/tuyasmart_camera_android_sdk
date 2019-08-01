# 涂鸦智能摄像机 Android SDK

[中文版](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README-zh.md) | [English](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README.md)

------

## 功能概述

涂鸦智能摄像头 SDK 提供了与远端摄像头设备通讯的接口封装，加速应用开发过程，主要包括了以下功能：

- 预览摄像头实时采集的图像。
- 播放摄像头SD卡中录制的视频。
- 手机端录制摄像头采集的图像。
- 与摄像头设备通话。
- 开放云存储功能

## 快速集成

**使用 AndroidStudio (版本号 3.1.3及更高版本)**

- 创建项目工程
- 在根目录build.gradle添加maven地址：
```java
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

- 在模块的build.gradle中添加如下代码:

```java
dependencies {
    ...
    // tuya camera module
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.11.1r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.11.0r119'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.11.0r119'
    
    implementation 'com.tuya.smart:tuyasmart-tuyaHybridContainer:1.0.0'
    implementation 'com.github.wendux:DSBridge-Android:3.0-SNAPSHOT'

    implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.11.0r119'

    //not required Compatible with older versions
    implementation "com.tuya.smart:tuyaCamera:3.11.0r119h2"

    implementation 'com.tuya.smart:tuyasmart:3.9.6'
}
```

AndroidStudio的使用请参考: [AndroidStudio Guides](https://developer.android.com/studio/)



## 开发文档

更多请参考: [涂鸦智能摄像机 Android SDK使用说明](https://tuyainc.github.io/tuyasmart_camera_android_sdk_doc/zh-hans/)

## 更新日志
- 支持arm64
- 支持云存储

