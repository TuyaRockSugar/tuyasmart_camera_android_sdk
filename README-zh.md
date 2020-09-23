# 涂鸦智能摄像机 Android SDK

[中文版](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README-zh.md) | [English](https://github.com/TuyaInc/tuyasmart_camera_android_sdk/blob/master/README.md)

------

## 功能概述

涂鸦智能摄像头 SDK 提供了与远端摄像头设备通讯的接口封装，加速应用开发过程，主要包括了以下功能：

- 预览摄像头实时采集的图像。
- 播放摄像头SD卡中录制的视频。
- 手机端录制摄像头采集的图像。
- 与摄像头设备通话。
- 支持云存储

## 快速集成

**使用 AndroidStudio (版本号 3.1.3及更高版本)**

- 创建项目工程
- 在根目录build.gradle添加maven地址：

```java

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

- 在模块的build.gradle中添加如下代码:

```java
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
    implementation 'com.tuya.smart:tuyasmart:3.17.8'

    // tuya camera module
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-middleware:3.17.0r139h1'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v2:3.17.0r139h3'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-utils:3.13.0r129h1'
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-message:3.13.0r128'
    implementation 'com.tuya.smart:tuyasmart-ipc-devicecontrol:3.17.0r139'

    // 用于兼容 tutk 设备
    implementation 'com.tuya.smart:tuyasmart-ipc-camera-v1:3.17.0r139h4'

    //消息中心图片解密组件
    implementation 'com.tuya.smart:tuyasmart-imagepipeline-okhttp3:0.0.1'
    implementation 'com.facebook.fresco:fresco:1.3.0'
}

repositories {
    mavenLocal()
    jcenter()
    google()
}
```

AndroidStudio的使用请参考: [AndroidStudio Guides](https://developer.android.com/studio/)



## 开发文档

更多请参考: [涂鸦智能摄像机 Android SDK使用说明](https://tuyainc.github.io/tuyasmart_home_android_sdk_doc/zh-hans/resource/ipc/)

## 更新日志
- 2020.9.23
   - 更新sdk，解决某些设备回放会跳帧问题
- 2020.5.20
   - 更新 sdk，使用新的播放器用于支持老设备（tutk）
   - 更新优化 demo
- 2020.5.9
   - 更新 sdk（版本 3.17.0r139），修复音频问题（切换清晰度声音关闭），so崩溃问题，提升sdk稳定性；增加设备所有dp点操作的上报回调
   - 更新优化 demo
- 2020.3.31
    - 更新底层库，修复armabi消息中心视频播放问题
- 2020.3.4
    - 更新sdk（版本 3.15.0r135），消息中心多媒体预览（云存储视频播放）
- 2019.11.15
    - 更新sdk(版本 3.13.0r129)，对应的ffmpeg是4.1.4版本
    - 更新sdk demo
- 2019.10.8
    - 更新sdk（版本 3.12.6r125）
- 2019.8.1
    - 支持云存储功能 （版本 3.11.1r119）
- 2019.7.13
    -  新的sdk代码重构，接口方法有变更，为了兼容老版本sdk请使用tuyaCamera:3.11.0r119h2。建议老用户向上升级
- 2019.6.11
    - 支持arm64

