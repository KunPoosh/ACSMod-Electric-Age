# 电动推进器导出说明

本体 32×32 px（2×2 格），桨叶 16×32 px（独立外伸件），16 px/格。各 PNG 均为原尺寸透明图，未裁切。桨叶相对本体偏移 (-16,0)，组合预览 48×32 不代表 3×2 占地。

## 部件与组合
| 文件 | 尺寸 | 用法 |
| --- | --- | --- |
| body.png / body_damaged.png | 32×32 | 正常/受损结构，互斥；指示灯未亮 |
| state_rated.png | 32×32 | 额定：蓝白常亮双指示 |
| state_derated.png | 32×32 | 降频：一个较暗、较小的蓝色指示 |
| state_overload.png | 32×32 | 实际过载：高亮指示、少量线圈热斑 |
| moving_00.png … moving_03.png | 16×32 | 四帧桨叶，已含固定轮毂和短轴 |
| moving_stopped.png | 16×32 | 独立停转姿态，与 moving_00 外观相同 |
| moving_damaged.png | 16×32 | 受损停转草图，缺角及弯折；未制作受损循环 |
| moving_sheet.png | 64×32 | 四帧桨叶从左到右，无间隔 |
| working/frame_00.png … frame_03.png | 48×32 | 正常本体 + 额定状态 + 四帧桨叶的完整预览导出 |
| working_sheet.png | 192×32 | 四帧完整预览从左到右，无间隔 |

合成时把本体放在 (0,0)，桨叶放在 (-16,0)，再将一个运行状态层放在本体 (0,0)。关机时不叠任何状态图，显示 moving_stopped。不要把三种状态图同时叠加，也不需要空白 state_off 文件。

propulsion.png 是正常额定完整展示；propulsion_damaged.png 是受损本体、受损停转桨和额定灯的结构组合示例，并非可运行的受损动画。stopped.png / stopped_damaged.png 为对应无灯停转图。完整展示已经包含部件和灯，不再重复叠层。

## 可编辑源文件
- ../source/source.aseprite：32×32，单帧，5 层。Structure_Normal / Structure_Damaged 互斥，State_Rated / State_Derated / State_Overload 互斥。默认正常 + 额定。
- ../source/propeller.aseprite：16×32，5 帧，3 层。Blades_Normal 为 4 个循环姿态与第 5 帧停转；Hub_Shaft 为固定部件；Blades_Damaged_Stop 仅第 5 帧有内容。检查损伤时选 Stopped，隐藏 Blades_Normal，显示 Blades_Damaged_Stop，保留 Hub_Shaft。
- ../previews/assembly.aseprite：48×32 的可编辑组合检查文档。Working 标签为第 1–4 帧，Stopped 为第 5 帧且无灯；原始部件仍以两个 source 文件为准，修改后重新合成。

Working 按 00→01→02→03→00 循环，每帧 100 ms。降频和实际过载复用相同四张图，由接入方放慢/加快播放；没有将播放时间当成实际转速或功率数值。侧视桨叶通过长度与扭转投影变化表示转动，不应直接把整张外伸图绕屏幕中心旋转。

## 预览与检查
- ../previews/propulsion-working-8x.gif：4 帧、100 ms、无限循环。
- ../previews/states-6x.png：上排正常结构、下排受损结构；每排依次关机、额定、降频、实际过载。桨叶姿态仅用于示意，运行速度待接入。
- ../previews/damage-comparison-8x.png：左为受损本体 + 完整桨，右为受损本体 + 受损桨；均无灯。
- ../previews/layout-12x.png：青蓝框为本体、黄框为外伸画布、白框为人员前景操作区意向、底部蓝线为通行意向、橙红十字为桨叶轴心。详见 anchors.txt，不能据此自动生成 canOccupy。
- verification.json：从磁盘重开两份源稿与组合文件，逐像素核对源层/帧及导出；核对 GIF 解码后与原尺寸帧的 8 倍最近邻结果完全一致。

尚未验证游戏加载、状态接口、人员路径、镜像、外伸遮挡、bump、碎片或残骸。
