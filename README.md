# 腾讯云数智人 Android 云端推流 SDK 使用示例

## 项目说明

app 目录为接入 sdk 的 demo 示例，其中此 demo 额外包含了腾讯云 ASR（语音识别）的示例，以及包含了基础的对话及消息展示的 UI 的示例，若要正常运行此 demo，需要在 app/src/main/java/Config 中配置您自己数智人项目的相关 key 和您腾讯云 ASR 相关的账号信息。

```Kotlin
// 数智人及ASR等全局参数
object Config {
    // 数智人key
    const val APP_KEY = "xxx"
    // 数智人token
    const val ACCESS_TOKEN = "xxx"
    // 数智人virtualmanProjectId
    const val VIRTUALMAN_PROJECT_ID = "xxx"

    // ASR账号 app id
    const val ASR_APP_ID = xxx
    // ASR账号 SecretId
    const val ASR_SECRET_ID = "xxx"
    // ASR账号 SecretKey
    const val ASR_SECRET_KEY = "xxx"
}
```
