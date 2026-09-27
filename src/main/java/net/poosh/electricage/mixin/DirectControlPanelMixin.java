/* DirectControlPanelMixin.java — 直接操控面板预留电力条及文字过载按钮行。 */
package net.poosh.electricage.mixin;

import com.zarkonnen.airships.*;
import com.zarkonnen.catengine.*;
import com.zarkonnen.catengine.util.*;
import net.poosh.electricage.client.ElectricUi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=DirectControlPanel.class,remap=false)
public abstract class DirectControlPanelMixin {
    @ModifyVariable(method="draw",at=@At("STORE"),name="h")
    private int electricAge$height(int height,MyDraw d,Pt cursor,ScreenMode sm,Hooks hooks,UniScreen us) {
        return height+ElectricUi.panelExtraHeight(us);
    }
    @Redirect(method="draw",at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/CommandButtonsPanel;renderShipQuantities(Lcom/zarkonnen/airships/MyDraw;Lcom/zarkonnen/airships/Airship;III)V"))
    private void electricAge$commands(MyDraw d,Airship ship,int x,int y,int width,
            MyDraw draw,Pt cursor,ScreenMode sm,Hooks hooks,UniScreen us) {
        CommandButtonsPanel.renderShipQuantities(d,ship,x,y,width);
        int bottom=y+MyDraw.BUTTON_H+MyDraw.BUTTON_SPACING*2+AGame.FOUNT.height+AGame.FOUNT.lineHeight*2;
        ElectricUi.overloadRow(d,us,x,bottom+ElectricUi.resourceRowHeight(us)+MyDraw.BUTTON_SPACING,width);
    }
}
