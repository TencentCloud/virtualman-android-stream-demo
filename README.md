# 腾讯云数智人 Android 云端推流 SDK 使用示例

## 项目说明

本项目是腾讯云数智人 Android SDK 的简化 demo 示例，展示了数智人云端推流的基本使用方法。

## 环境要求

- Android Studio
- Android SDK API 21+
- 目标 API 33+
- 支持架构：armeabi-v7a, arm64-v8a

## SDK 版本

- 数智人 SDK：2.2.6
- TRTC SDK：13.0.0.19666

## 配置说明

运行 demo 前，需要在 `app/src/main/java/com/tencent/virtualman_demo_app/Config.kt` 中配置您的数智人项目信息：

```kotlin
object Config {
    // 数智人 AppKey
    const val APP_KEY = "your_app_key"
    
    // 数智人 AccessToken  
    const val ACCESS_TOKEN = "your_access_token"
    
    // ========== 建流方式1: AssetVirtualman ==========
    // 数智人形象 Key
    const val ASSET_VIRTUALMAN_KEY = "your_asset_virtualman_key"
    
    // ========== 建流方式2: VirtualmanProject ==========
    // 数智人项目 ID
    const val VIRTUALMAN_PROJECT_ID = "your_virtualman_project_id"
}
```

## 建流方式

SDK 支持两种建流方式：

1. **AssetVirtualman**：使用数智人形象 Key 直接建流
2. **VirtualmanProject**：使用数智人项目 ID 建流

根据您的业务需求选择对应的方式，并配置相应的参数。

## 运行步骤

1. 在数智人平台获取您的 AppKey、AccessToken 等配置信息
2. 替换 `Config.kt` 中的配置参数
3. 编译运行项目
