# 电动推进器（propulsion）制作记录

## 任务与版本
- 日期：2026-09-27。状态：美术绘制与导出完成，待游戏接入。
- 用户授权：沿用发电机、蓄电池流程，继续完成电动推进器。
- 需求：ART_SPEC.zh-CN.md v1.0（上级 README 所列路径与 SHA256），§2–5、§7。占地与人数为确认基线，帧数/拆层按接受的首轮默认执行。
- 本次为 ASC「电气时代」工作副本，16 px/格；不套用 ArmorVsAmmo 20 px/格或 Unity PPU/命名规范。
- 作者/制作方法：本会话按委托使用 Aseprite MCP、Lua Image API 原创像素绘制；没有使用生图模型或缩图导入。
- 参考：已完成的发电机、蓄电池与此前用户提供的 ASC 画风。未复制原版图块；原版参考版权仍归原权利人，本记录不授予其再分发许可。
- 阶段稿：source/propulsion-blockout.aseprite、source/propeller-blockout.aseprite 保留。
- 既有发电机与蓄电池均有未保存标记，本轮未保存/编辑它们；导出时比对现场第一帧与起始快照一致。

## 身份、尺寸与坐标
- 本体 2×2 格、32×32 px；1 名直接操作员。外伸桨叶 16×32，偏移 (-16,0)；组合 48×32 不新增占地。
- 本体左上原点 (0,0)，X 右、Y 下。桨叶局部轴心 (8,16)，在本体坐标为 (-8,16)；连接边界位于本体 (0,16)。
- 相同部件的帧/状态保持同画布和原点；未裁切、未平滑缩放。透明轮廓随桨叶投影改变属于动画本身。
- 正式 ID、作者绑定、canOccupy、物理 mask、接口排序未提供，不从可见像素反推。无 Unity .meta/GUID 操作。
- 人员前景操作区、底部通行和出入口意向见 exports/anchors.txt、previews/layout-12x.png。它们是草案标注，未画入产品颜色图。
- 主体以机架和横轴电机表现，有功能依据的开架留白；不强套实体箱体的连续封闭壳体。

## 视觉设计
- 灰钢机架与轴承、横置铜绕组、黄铜环、顶部陶瓷端子，前下方控制杆与指示槽。
- 左侧双叶桨通过侧视长度和扭转变化产生转动；轮毂、短轴不漂移。
- 只绘材料/局部结构明暗，没有世界方向投影、辉光模糊、人物或 UI 文本。
- 关机：灯灭且停转；额定：蓝白双指示；降频：单点暗蓝；实际过载：更亮的指示与少量绕组热斑。
- 正常/受损与运行状态分离。主体损伤为绕组破损、端盖裂痕、绝缘子缺损；桨叶另有缺角与弯折的静态损伤草图。
- 法线、独立光照遮罩、LightOccluder、bump、碎片和残骸不在本轮交付。

## 图层与导出映射
| 源文件 / 图层 | 职责 | 导出 |
| --- | --- | --- |
| source.aseprite / Structure_Normal | 静态正常本体 | body.png |
| source.aseprite / Structure_Damaged | 静态受损本体，替换正常层 | body_damaged.png |
| source.aseprite / State_Rated | 额定指示 | state_rated.png |
| source.aseprite / State_Derated | 降频指示 | state_derated.png |
| source.aseprite / State_Overload | 实际过载指示及热斑 | state_overload.png |
| propeller.aseprite / Blades_Normal | 4 个旋转姿态 + 停转 | 与轮毂合成 moving_00..03.png、moving_stopped.png |
| propeller.aseprite / Hub_Shaft | 固定轮毂与短轴 | 包含于所有 moving PNG |
| propeller.aseprite / Blades_Damaged_Stop | 第 5 帧受损桨草图 | 与轮毂合成 moving_damaged.png |

- 两份主源稿位于 source/；previews/assembly.aseprite 只用于组合检查，保留本体、桨叶、指示独立层。
- exports/README.md 解释所有原尺寸部件、完整展示图与帧表的用途，避免完整图重复叠加状态。
- 默认源稿为正常 + 额定；损伤层隐藏。查看受损桨应选第5帧、隐藏正常桨并启用损伤桨。
- 实际图层/PNG 均已生成，无空白占位资产。

## 动画与程序运动
- Working 为 1–4 帧、100 ms/帧，00→01→02→03 循环；Stopped 为第5帧，与首帧外观相同但用途独立。
- 只有桨叶变化；固定轮毂/短轴及电机本体不动。关机组合中灯光层无 cel。
- 降频/过载复用四帧，通过程序更慢/更快播放；本轮未制作额外受损运动循环，也未声称完成运行时驱动。
- 不把帧时间等同于功率或实际每分钟转速。屏幕内整图旋转不适用于此侧视桨叶表现。

## 视觉迭代记录
| 阶段 | 实际读图与处理 | 结果 |
| --- | --- | --- |
| 轮廓 | 读取 10 倍组合草图，检查横轴接合、铜电机与独立双叶桨 | 对齐成立；轮廓保留，继续细节 |
| 细节与动画 | 补端盖散热槽、底座螺栓、控制杆和状态指示，拆开固定轮毂；读取原尺寸首帧和 8 倍五姿态图 | 4 个姿态各异，停转明确，轴心稳定 |
| 状态与损伤 | 读取正常/受损两排四状态图，以及完整桨/受损桨对照 | 损伤与灯光独立，未把受损等同关机 |
| 整套对照 | 读取三模块同尺度预览 | 色板、密度与底座尺度协调；未完成实际整船检查 |

## 验收与证据
- 已验证：两份主源稿及组合文件从磁盘可重开；尺寸、16×16 网格、5/3 层和 1/5 帧正确。
- 已验证：原尺寸透明 PNG 逐像素对应源层/帧；Alpha 为硬边 0/255；母体损伤保持 Alpha 轮廓，状态像素均落在本体结构范围内。
- 已验证：轮毂各帧一致，cel 原点一致，四姿态互不相同，100 ms 与 Working/Stopped 标签正确。
- 已验证：原生 Aseprite 导出 GIF 为 4 帧、100 ms、无限循环；逐帧解码与 8 倍最近邻 PNG 的可见像素一致。
- 已实际查看：原尺寸首帧、10 倍草图、8 倍姿态带、6 倍状态表、8 倍损伤表及三件同尺度组合。
- 证据：exports/verification.json；scripts/01–04；previews/。
- 待验证：真实游戏加载、外伸遮挡、镜像、状态切换及调速、人员寻路与整船视野。未执行 Unity/D4/灯光或法线验收。

## 交付与待办
- 可编辑主文件：source/source.aseprite、source/propeller.aseprite。
- PNG、帧表、锚点和导出说明：exports/。
- 预览：previews/propulsion-working-8x.gif、propulsion-preview.png、states-6x.png、damage-comparison-8x.png、layout-12x.png。
- 上级已完成资产总览：../../previews/completed-modules-3of6.png。
- 后续接入时确认人员区、外伸排序、状态控制和实际播放节奏；受损桨当前仅提供静态草图。
