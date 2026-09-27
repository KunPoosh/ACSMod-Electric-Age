/* CommandPanelMixin.java — 原版煤炭/弹药资源区域增加一行，并同步面板高度与滑入位置。 */
package net.poosh.electricage.mixin;
import com.zarkonnen.airships.*;
import com.zarkonnen.catengine.*;
import com.zarkonnen.catengine.util.*;
import net.poosh.electricage.client.ElectricUi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=CommandButtonsPanel.class,remap=false)
public abstract class CommandPanelMixin {
 @Shadow public int panelW;
 @Shadow public int panelH;
 @Shadow public int panelX;
 @Shadow private int moveInH;
 @ModifyVariable(method="draw",at=@At("STORE"),name="h")
 private int electricAge$drawHeight(int height,MyDraw d,Pt cursor,ScreenMode sm,Hooks hooks,UniScreen us){return height+ElectricUi.panelExtraHeight(us);}
 @ModifyVariable(method="tick",at=@At("STORE"),name="h")
 private int electricAge$tickHeight(int height,Input in,int ms,UniScreen us){return height+ElectricUi.panelExtraHeight(us);}
 @Inject(method="draw",at=@At("TAIL"))
 private void electricAge$commands(MyDraw d,Pt cursor,ScreenMode sm,Hooks hooks,UniScreen us,CallbackInfo ci){
  if(us.combat==null || us.hideUI || us.combat.startCountdown>0 || us.tool!=UniScreen.NAVIGATE
      || DirectControlPanel.getShip(us)!=null)return;
  ElectricUi.overloadRow(d,us,panelX+MyDraw.WINDOW_INSET,moveInH+panelH-ElectricUi.overloadRowHeight(us),panelW-MyDraw.WINDOW_INSET*2);
 }
 @Inject(method="renderShipQuantities",at=@At("RETURN"))
 private static void electricAge$resources(MyDraw d,Airship ship,int x,int y,int width,CallbackInfo ci){
  ElectricUi.resourceRow(d,ship,x,y+MyDraw.BUTTON_H+MyDraw.BUTTON_SPACING*2+AGame.FOUNT.height+AGame.FOUNT.lineHeight*2,width);
 }
}
