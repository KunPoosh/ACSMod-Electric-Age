# 参与电气时代开发

[English](CONTRIBUTING.md) · [项目说明](README.zh-CN.md) · [文档索引](docs/README.md)

## 本地准备

需要 JDK 21、自己拥有的 Airships 游戏安装，以及包含 `core/`、`loader-libs/` 的 Acbric 发行目录。复制 `local.properties.example` 为 `local.properties` 并配置两个路径；不提交本机路径、游戏文件或框架二进制。

- `gradlew.bat jar --no-daemon`：只编译与打包，不运行测试或游戏。
- `gradlew.bat build --no-daemon`：编译、打包和核心回归，不启动游戏。
- `python tests/verify_package.py`：检查已构建的 JAR。

Linux/macOS 使用 `./gradlew`。运行期脚本目前按 Windows 环境编写；不要假定其跨平台可用。`tests/run_runtime.py` 和 `tests/run_game.py` 会启动隔离的 JVM/游戏进程，须显式选择执行；游戏探针还依赖 Acbric 开发仓库的工具与测试产物，普通发行包不足以运行这些探针。参数见脚本 `--help`，验证边界见 [TESTING](TESTING.zh-CN.md)。

## 修改约定

- 功能代码位于 `src/main/java/`；原版模块属性在 `content/devices.json`，电网和闪电参数在 `src/main/resources/electric_age/balance.properties`。数值变化同步中英文 BALANCE。
- 美术源文件及导出部件在 `electric_age_art/modules/`。Gradle 合成 `build/generated/content/`；修改输入，不手改生成目录。普通构建不需要 Aseprite。
- 玩家文案与主要文档保持中英文一致。精确 Mixin 需核实目标游戏版本；不要把编译成功描述为实机通过。
- 不打包游戏、框架、研究反编译资料、存档、日志或个人路径。不要覆盖玩家安装目录。
- PR 说明具体问题、改后表现及验证范围；没有运行的测试明确写出。提交范围保持聚焦。

自有贡献按项目 MIT 许可证提供。第三方内容须保留许可与出处；游戏素材仅在运行时引用，不复制进仓库。报告问题时附 MOD/Acbric/游戏版本、复现步骤和相关日志片段，并去掉私密信息。
