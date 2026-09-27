# 发电机导出说明

全部单帧/单层 PNG 为 48×32 RGBA，原点 (0,0)，保留完整透明画布；两张横排 Sheet 均为 192×32。没有自动裁边、平滑缩放或预乘背景。

## 可直接查看的完整外观
- `generator.png`：正常运行第 1 帧。
- `generator_damaged.png`：受损结构 + 同一运行帧，炉火、仪表和运动件仍可工作。
- `working/frame_00.png`～`frame_03.png`：完整 4 帧工作循环。
- `working_sheet.png`：上述 4 帧从左到右排列，每帧 100 ms，共 400 ms，循环 00→01→02→03→00。
- `stopped.png` / `stopped_damaged.png`：正常/受损停止外观。转子和连杆静止，仪表低位，工作灯熄灭，炉内仅留余烬。

## 分层接入
**`body.png` 是结构母层，不是完整合成外观。** 下列顺序从后到前叠合：

1. `body.png` **或** `body_damaged.png`：两者互斥，不要同时显示。
2. `furnace_working.png` / `furnace_stopped.png`。
3. `moving_00.png`～`moving_03.png` / `moving_stopped.png`：Rotor 与 Piston_Rod 两层合并，不重复叠到完整合成外观上。
4. `gauge_working.png` / `gauge_stopped.png`。
5. `fittings_over.png`：炉箅、门闩、轴承盖、外侧压板。
6. `status_working.png` / `status_stopped.png`。

`moving_sheet.png` 是四帧运动部件的横排图。`layers/` 保存第 1 帧的八个单层文件，便于修改或拆分接入；需要运动时不能只使用其中的静态 Rotor/Piston_Rod。

源文件中：Working 标签为第 1～4 帧；Stopped 标签仅第 5 帧。请按标签/上述序列播放，不把整份 5 帧源文件连续循环。源文件默认隐藏 Structure_Damaged。

没有为发电机创建 rated/derated/overload 四种用电负载状态。炉火为独立静态工作母层，未来可替换为原版粒子；本轮不添加烟雾、bump、碎片或完整残骸。

锚点与人员布局见 `anchors.txt`。这里的导出清单不代表已实现游戏状态接口或已完成实际加载验收。

制作：本会话通过 Aseprite MCP / Lua Image API 原创绘制。视觉参考为用户先前提供的 ASC 原版图集与 ART_SPEC v1.0；未在此导出包中复制原版图块。原版参考的版权仍属原权利人，本记录不授予其再分发许可。
