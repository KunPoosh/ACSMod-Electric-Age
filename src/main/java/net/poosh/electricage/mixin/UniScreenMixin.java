/* UniScreenMixin.java — 随原版组件重建添加纯视觉电弧，指令入口位于舰船面板。 */
package net.poosh.electricage.mixin;
import com.zarkonnen.airships.UniScreen;
import net.poosh.electricage.client.ElectricUi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=UniScreen.class,remap=false)
public abstract class UniScreenMixin {
    @Inject(method="reloadComponents",at=@At("RETURN"))
    private void electricAge$ui(CallbackInfo ci){ElectricUi.install((UniScreen)(Object)this);}
}
