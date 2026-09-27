# 电气时代 / Electric Age

基于 Acbric 的独立 Airships: Conquer the Skies Java MOD，探索发电、储能和真实耗电设备。

**当前版本：0.1.0-dev.1，项目骨架。没有实现电力玩法。**

- MOD ID：`electric_age`
- Java 包：`net.poosh.electricage`
- 构建环境：JDK 21、Gradle Wrapper 8.13
- 声明的框架下限：Acbric API `0.3.3-dev.32`；不表示所有后续版本已验证
- 初始化只记录日志，不修改原版资源、模块、战役或存档
- 独立于 ARC Overhaul；没有 ARC 依赖，也不加入框架默认发行

[English](README.md) · [GDD 讨论草案](docs/GDD.zh-CN.md) · [验证范围](TESTING.zh-CN.md) · [第三方声明](THIRD_PARTY_NOTICES.md)

## 构建

1. 准备 JDK 21、自有游戏安装与 Acbric 发行目录。
2. 将 `local.properties.example` 复制为 `local.properties`，填写游戏目录和包含 `core`、`loader-libs` 的框架目录。
3. 在此目录执行 `gradlew.bat build`；Linux/macOS 可使用 `./gradlew build`。

路径也可用 `-PframeworkDir=... -PgameInstallDir=...` 覆盖。输出为 `build/libs/Electric-Age-0.1.0-dev.1.jar`。构建不会启动游戏或自动安装 MOD，依赖只参与编译，不进入 JAR。

当前没有自动玩法测试，Gradle 的 `test NO-SOURCE` 不代表功能验证通过。游戏版本及测试边界见 TESTING。

## 设计与许可状态

GDD v0.1 为讨论草案：名称与发电/储能/耗能方向已确定，具体范围、供电规则、战役及联机行为待讨论；草案不代表实现授权或完成状态。

本项目自有代码与文档的许可证待所有者确定，本次未自动沿用其他项目的许可。Gradle Wrapper 保留其上游许可，见第三方声明。
