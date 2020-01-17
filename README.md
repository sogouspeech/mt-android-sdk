# 欢迎使用搜狗知音在线翻译sdk
## 一、搜狗知音官网注册

开发者首先要在[搜狗知音官网](https://zhiyin.sogou.com/)注册开发者信息，然后注册app信息获取appId、appKey。在线翻译支持中文翻译为英文、日文、韩文、法文、西班牙文、俄文、德文，以及英文、日文、韩文、法文、西班牙文、俄文、德文翻译为中文，可参照[在线翻译demo](https://github.com/sogouspeech/mt-android-demo)进行配置，语种参数如下：
```
 public static final String CHINESE = "zh-cmn-Hans";
 public static final String ENGLISH = "en";
 public static final String JAPANESE = "ja";
 public static final String KOREAN = "ko";
 public static final String FRENCH = "fr";
 public static final String SPANISH = "es";
 public static final String RUSSIAN = "ru";
 public static final String GERMAN = "de";
```

## 二、依赖配置
1、在项目根gradle中进行如下配置：
```
buildscript {
    repositories {
        google()
        jcenter()

    }
    dependencies {
        //引用github的android-maven-gradle插件，用于从github拉取代码
        classpath 'com.github.dcendents:android-maven-gradle-plugin:1.5'
        //引用protobuf-gradle插件
        classpath "com.google.protobuf:protobuf-gradle-plugin:0.8.8"
    }
}
allprojects {
    repositories {
        google()
        //配置jitpack远程库
        maven { url 'https://jitpack.io' }
        jcenter()

    }
}
```
2、在app的gradle中进行如下配置：
```
//应用google的protobuf插件
apply plugin: 'com.google.protobuf'
//应用github的dcendents.android-maven插件，用于从github拉取代码
apply plugin: 'com.github.dcendents.android-maven'
ext {
    //grpc版本
    grpcVersion = '1.22.1'
}
dependencies {
    //引用grpc依赖
    implementation("io.grpc:grpc-protobuf:${grpcVersion}") {
        exclude module: 'jsr305'
    }
    implementation("io.grpc:grpc-stub:${grpcVersion}") {
        exclude module: 'jsr305'
    }
    implementation("io.grpc:grpc-auth:${grpcVersion}") {
        exclude module: 'jsr305'
    }
    implementation("io.grpc:grpc-okhttp:${grpcVersion}") {
        exclude module: 'jsr305'
    }
    //加密库
    implementation 'org.conscrypt:conscrypt-android:1.4.2'
    //在线翻译库
    implementation 'com.github.sogouspeech:mt-android-sdk:1.0.3'
    //网络库+工具类
    implementation 'com.github.sogouspeech:common-android-sdk:1.0.3'
}
```
## 三、权限配置
只需配置网路权限，如下：
```
<uses-permission android:name="android.permission.INTERNET" />
```
