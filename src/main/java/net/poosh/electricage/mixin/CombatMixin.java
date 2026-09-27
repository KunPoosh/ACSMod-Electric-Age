/* CombatMixin.java — 注册新命令，不跳过原版录制、校验与有序执行。 */
package net.poosh.electricage.mixin;

import com.zarkonnen.airships.Combat;
import net.poosh.electricage.integration.OverloadCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Combat.class,remap=false)
public abstract class CombatMixin {
    @Inject(method="<clinit>",at=@At("RETURN"))
    private static void electricAge$commands(CallbackInfo ci){OverloadCommands.register(Combat.EXECS);}
}
