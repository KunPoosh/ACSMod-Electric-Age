# 电气浮力装置导出说明

48×32 px，3×2 格，16 px/格。所有部件、状态和单帧 PNG 均保持完整 48×32 透明画布与 (0,0) 原点，不裁切。装置通过两侧线圈给中央蓝色浮晶充能，由浮晶产生升力；关机和受损时仍保留蓝色晶体。

## 源稿与组合顺序
源文件：../source/source.aseprite，8 层、5 帧。组合时依次叠加：
1. body.png 或 body_damaged.png：机架、供电线圈、陶瓷端子、承托座和控制面板，互斥。
2. moving_00.png … moving_03.png：正常浮晶；或 crystal_damaged/frame_00.png … frame_03.png：对应帧的裂纹浮晶，互斥。
3. charge/frame_00.png … frame_03.png：端子向浮晶传入的充能脉冲，与浮晶帧索引同步。
4. state_rated.png / state_derated.png / state_overload.png：额定、降频、实际过载指示，三者互斥。

机架损伤与晶体损伤可以独立选择；均不代表自动关机。关机时使用 moving_stopped.png 或 moving_damaged_stopped.png，隐藏充能脉冲和所有状态光。没有空白 state_off 文件。

| 源层 | 职责 | 导出 |
| --- | --- | --- |
| Structure_Normal | 正常机架与供电组件 | body.png |
| Structure_Damaged | 受损机架与供电组件 | body_damaged.png |
| Crystal_Normal | 正常蓝色浮晶 | moving_00..03.png、moving_stopped.png |
| Crystal_Damaged | 裂纹蓝色浮晶 | crystal_damaged/frame_00..03.png、moving_damaged_stopped.png |
| Charge_Pulse | 同步充能脉冲 | charge/frame_00..03.png |
| State_Rated | 蓝白双指示 | state_rated.png |
| State_Derated | 较小暗蓝单指示 | state_derated.png |
| State_Overload | 明亮指示与局部线圈热斑 | state_overload.png |

默认可见为 Structure_Normal、Crystal_Normal、Charge_Pulse、State_Rated。第5帧没有充能和状态光 cel，直接显示关机。查看受损图时按需替换机架或浮晶层，不能同时显示两套母层。

## 动画与完整展示
- Working：第1–4帧，00→01→02→03循环，100 ms/帧；晶体垂直偏移为 0、-1、-1、0 px，晶面亮纹和端子脉冲随帧变化。
- Stopped：第5帧，晶体偏移 +1 px、落近承托座；仍为蓝色，停止亮纹、充能和指示。
- 降频/实际过载复用相同四帧，由接入方放慢/加快节奏并选择对应状态层。100 ms 是交接默认值，不定义功率、升力或物理运动参数。
- moving_sheet.png、charge_sheet.png、working_sheet.png 均为192×32，四帧从左到右，无间隔。
- working/frame_00.png … frame_03.png：正常结构 + 正常浮晶 + 充能 + 额定指示的完整工作图。
- lift.png / lift_damaged.png：第1帧正常/受损完整额定展示，受损展示同时替换机架和晶体。
- stopped.png / stopped_damaged.png：完整关机参考。完整图已经合成，勿再重复叠加部件或状态。

## 预览与证据
- ../previews/lift-working-8x.gif：4帧，100 ms，无限循环，最近邻放大8倍。
- ../previews/states-6x.png：上排正常、下排受损；每排关机→额定→降频→实际过载。静态表不能展示转速差异。
- ../previews/layout-12x.png：蓝框为本体画布；黄框为晶体运动包络；橙红十字为基准点；白框为操作员前景区域意向；底部蓝线及两端浅蓝线为通行/入口意向。
- anchors.txt：晶体基准、运动范围、供电端点、1名操作员位置意向。布局不自动成为 canOccupy 或物理 mask。
- verification.json：从磁盘重开源稿，对照源层/PNG，检查静态机架、透明轮廓、晶体蓝色与运动范围、帧时长、停机无灯，以及 GIF 逐帧像素一致性。
- preservation-check.json：原先打开的5份文档首帧与修改标记保持不变；本轮未保存这些文档。

游戏加载、升力逻辑、状态切换、人员路径、镜像、bump、碎片和残骸均待接入验证。
