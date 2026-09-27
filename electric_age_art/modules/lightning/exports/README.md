# 线圈电塔导出说明

32×48 px，2×3格，16 px/格。露天甲板式固定线圈塔，顶部放电，无包覆全塔的舰体装甲。此次按用户要求制作塔身外观，源稿单帧，无方向帧或整条电弧动画。

## 分层与组合
源文件：../source/source.aseprite，7层。所有PNG保持32×48完整透明画布，原点(0,0)，不裁切。

| 文件 | 源层 | 用法 |
| --- | --- | --- |
| body.png | Structure_Normal | 正常底座、支撑、绝缘件及铜线圈 |
| body_damaged.png | Structure_Damaged | 对应受损结构，替换body |
| terminal.png | Terminal_Normal | 独立顶部环形电极与放电柱 |
| terminal_damaged.png | Terminal_Damaged | 受损电极，替换terminal |
| state_rated.png | State_Rated | 蓝白顶端指示与控制盒双指示 |
| state_derated.png | State_Derated | 小面积暗蓝单指示、较弱顶端 |
| state_overload.png | State_Overload | 更亮的顶端/控制指示及局部绕组热斑 |

顺序为body→terminal→一个状态层，全部对齐(0,0)。正常/受损母层互斥，机架与电极损伤可独立选择。关机时保留结构和电极，隐藏所有状态层；不交空白state_off文件。

lightning.png / lightning_damaged.png为完整额定展示；stopped.png / stopped_damaged.png为完整关机参考。完整图已含对应部件，勿重复叠加。源稿默认显示正常结构、正常电极和额定状态，其余隐藏。

## 放电与交接边界
- 顶部放电锚点为本体(16,2) px，正常/受损保持一致；坐标见anchors.txt。
- 塔身固定朝上，程序根据目标绘制电弧，不旋转塔身或套用右向炮管。
- v1.2已确认顶部安装、无舰体装甲覆盖、点命中不做电弧中途遮挡；这些是上游规则，本次未实现或运行验证。
- 本轮不画固定长度电弧或可选3帧亮闪，保持单帧外观。运行状态层不等于已经绑定的程序状态接口。
- 可见透明背景不代表不受击，不由Alpha生成命中mask。正式mask、人员路径与其他绑定待接入方处理。

## 预览与核对
- ../previews/lightning-preview.png：10倍默认展示。
- ../previews/states-6x.png：上排正常、下排受损；从左到右关机、额定、降频、实际过载。
- ../previews/layout-10x.png：蓝框为画布，红十字为放电锚点，白框为操作员前景区域意向，底部蓝线/两端浅蓝标记为通行与入口意向。标注不进入生产PNG。
- verification.json：磁盘重开、尺寸/层数/原点、源层与PNG、损伤Alpha、状态覆盖及锚点一致性均通过。
- preservation-check.json：其他10份已打开文档按Sprite ID核对首帧和修改标记不变，包含同名的两个火炮预览文档；本轮未保存它们。
- ../../../previews/completed-modules-6of6.png：六件资产同尺度总览。

尚未验证真实游戏加载、电弧逻辑、命中mask、人员寻路、镜像、整船视野、bump或碎片。
