# 电气时代 / Electric Age

基于 Acbric 的独立 Airships: Conquer the Skies Java MOD。**0.1.0-dev.4 已实现六设备单机战斗测试版本**：燃煤发电机、蓄电池、电动推进器、电气浮力装置、电气火炮、链式闪电塔。

全舰供电减少末端燃料搬运，但电网失效可能使多个系统同时离线。火炮保留弹药后勤；闪电无弹药、仍需装填。原版设备继续使用原有规则。独立于 ARC Overhaul，不加入 Acbric 默认发行。

[English](README.md) · [安装与实测](docs/PLAYTEST.zh-CN.md) · [验证范围](TESTING.zh-CN.md) · [GDD](docs/GDD.zh-CN.md) · [数值总表](docs/BALANCE.zh-CN.md) · [美术规格](docs/ART_SPEC.zh-CN.md)

## 已实现

- 整船功率分配、欠电降频与逐个关机、足额重启、电池额定支撑与均匀过载。
- 武器/动力过载开关使用原版指令；开关均扣指令，仅实际过载产生模块自损，保留原版起火与殉爆风险。
- 模块电量、启停和损伤余量保存；断裂继承开关与自身电量，俘获清除请求，原版补给入口充满电池。
- 瞬时链式闪电：原版瞄准、表面目标、能量派生、单点爆炸伤害和程序电弧。
- 六设备状态贴图、底部舰船面板的两个文字过载按钮、电力资源条和三项建造统计。

以下探针记录属于 dev.3 及更早版本；dev.4 按用户要求仅编译打包，未重跑自动测试；用户随后确认本轮 BUG 均已正常，见 [本轮修复说明](docs/COMBAT_FIX.zh-CN.md)。

历史自动探针已覆盖真实加载、战斗、原生序列化、部分维修/补给入口和 GPU 绘制。**完整战役连续作战与补给闭环、实际联机、长时间人工游玩尚未验收**。不扩展 AI 操作或自动结算算法。[实施顺序](docs/IMPLEMENTATION_PLAN.zh-CN.md) 保留这些后续阶段。

## 构建

需要 JDK 21、自己的游戏和 Acbric 发行目录。将 `local.properties.example` 复制为 `local.properties`，填写游戏目录及包含 `core`、`loader-libs` 的框架目录，然后执行 `gradlew.bat build --no-daemon`（其他系统用 `./gradlew`）。可用 `-PframeworkDir=... -PgameInstallDir=...` 覆盖路径。

产物：`build/libs/Electric-Age-0.1.0-dev.4.jar`。构建必跑 `coreTest`，不会启动游戏、安装 MOD 或提交 Git；依赖不进入 JAR。

MOD ID 为 `electric_age`，Java 包为 `net.poosh.electricage`。框架声明下限为 Acbric API `0.3.3-dev.32`；实际验证版本见 TESTING，不推断所有后续版本兼容。

数值设计权威为 BALANCE；机器配置为 `content/devices.json`（原版属性）和 `src/main/resources/electric_age/balance.properties`（电网/攻击）。修改数值时同步设计表。生产图集由素材原稿合成，不改母稿。

自有代码、文档与原创美术采用 [MIT 许可证](LICENSE)。Gradle Wrapper 保留上游许可，见 [第三方声明](THIRD_PARTY_NOTICES.md)。未自动提交、推送、发布或覆盖玩家安装。

[dev.3 渲染修复与更新说明](docs/RENDER_FIX.zh-CN.md)

## 仓库目录

| 目录 | 用途 |
| --- | --- |
| `src/main/` | Java 实现、Mixin 与运行配置 |
| `src/test/`、`tests/` | 核心回归、包检查与显式运行的隔离探针 |
| `content/` | 原版模块属性输入 |
| `electric_age_art/` | 原创美术源文件和构建所需导出部件 |
| `gradle/` | Wrapper 与资源合成逻辑 |
| `docs/` | 中英文设计、数值、美术和实测文档 |
| `licenses/` | 第三方许可证与通知 |

[文档索引](docs/README.md) · [参与开发](CONTRIBUTING.zh-CN.md) · [更新记录](CHANGELOG.zh-CN.md)

本机依赖路径、构建包、游戏、存档、日志及美术过程文件由 Git 忽略，保留在本地。GitHub 仓库不需要包含这些文件即可从正式 PNG 输入构建。
