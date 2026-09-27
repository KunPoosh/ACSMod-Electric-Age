/* ModuleTypeMixin.java — 只为本 MOD 解析实例绘制令牌；原版资源等级含义保持原样。 */
package net.poosh.electricage.mixin;
import com.zarkonnen.airships.*;
import net.poosh.electricage.client.ModuleArt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ModuleType.class,remap=false)
public abstract class ModuleTypeMixin {
    @Inject(method="getApp",at=@At("HEAD"),cancellable=true)
    private void electricAge$appearance(BonusSet bonuses,double level,CallbackInfoReturnable<Appearance> ci) {
        Appearance app=ModuleArt.appearance((ModuleType)(Object)this,level);if(app!=null)ci.setReturnValue(app);
    }
}
