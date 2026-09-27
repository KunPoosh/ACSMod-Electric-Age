# 电气时代美术 / Electric Age artwork

本目录包含六类设备的原创美术，采用仓库根目录的 [MIT 许可证](../LICENSE)。游戏原版素材不在此目录再分发。

This directory contains original artwork for six devices under the repository [MIT license](../LICENSE). Native game assets are not redistributed here.

## 目录 / Layout

- `modules/<device>/source/`：最终可编辑 Aseprite 源文件 / final editable Aseprite files.
- `modules/<device>/exports/`：原尺寸 PNG 部件、动画帧与锚点说明，供 Gradle 构建 / source-sized parts, animation frames and anchor notes used by Gradle.
- `modules/<device>/asset-notes.md`：制作阶段的来源、尺寸与设计记录，保留当时语境 / historical provenance, dimensions and production notes.

制作过程的 `previews/`、`scripts/`、`checks/`、阶段草图和检查 JSON 保留于本机并由 Git 忽略。其中脚本依赖原制作环境，不是项目构建所需工具。普通构建直接读取 PNG，不需要 Aseprite。

Production previews, scripts, checks, blockouts and verification JSON remain local and are ignored by Git. Those scripts depend on the original art workspace and are not build tools. Normal builds read exported PNGs without requiring Aseprite.

六类目录为 `generator`、`battery`、`propulsion`、`lift`、`cannon`、`lightning`。资源生成逻辑在 [content.gradle](../gradle/content.gradle)，规格见 [中文](../docs/ART_SPEC.zh-CN.md) / [English](../docs/ART_SPEC.md)。生成图集仅进入 `build/` 与 MOD JAR。

The six directories are generator, battery, propulsion, lift, cannon and lightning. See [content.gradle](../gradle/content.gradle) and the art specifications above; generated atlases go only into build outputs and the MOD JAR.

素材已接入 dev.4；用户已确认渲染正常。实际验证范围见 [TESTING](../TESTING.zh-CN.md)，不把历史“待接入”制作记录视作当前状态。

Artwork is integrated in dev.4 and the user has confirmed rendering. See [TESTING](../TESTING.md) for validation limits; historical production notes predate integration.
