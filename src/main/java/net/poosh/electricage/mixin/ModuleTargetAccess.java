/* ModuleTargetAccess.java — 复用原版目标选择及散布，不复制其加成逻辑。 */
package net.poosh.electricage.mixin;
import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import com.zarkonnen.catengine.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Module.class,remap=false)
public interface ModuleTargetAccess {
    @Invoker("target") Utils.Pair<Airship,Pt> electricAge$target(Combat c,double x,double y);
    @Invoker("jitter") double electricAge$jitter(Combat c,double value,double distance,boolean vertical,double mult,double fixed,Airship target,double fleet);
}
