/* ShipEditorMixin.java — 在原版建造统计列表中追加电力、容量续航和三组过载需求。 */
package net.poosh.electricage.mixin;
import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import com.zarkonnen.catengine.util.Utils;
import net.poosh.electricage.integration.ElectricRuntime;
import net.poosh.electricage.simulation.*;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ShipEditorUtils.class,remap=false)
public abstract class ShipEditorMixin {
    @Inject(method="getStats",at=@At("RETURN"))
    private static void electricAge$stats(Airship ship,int budget,Airship original,CallbackInfoReturnable<ArrayList<Utils.Pair<String,String>>> ci) {
        double rated=0,generation=0,capacity=0,discharge=0,weapons=0,propulsion=0;boolean found=false;
        for(Module m:ship.modules) {
            String key=ElectricRuntime.key(m);if(key==null)continue;found=true;var s=Balance.DEFAULT.spec(key);
            rated+=s.rated();generation+=s.generation();capacity+=s.capacity();discharge+=s.discharge();
            if(s.group()==DeviceSpec.Group.WEAPONS)weapons+=s.overload()-s.rated();
            if(s.group()==DeviceSpec.Group.PROPULSION)propulsion+=s.overload()-s.rated();
        }
        if(!found)return;
        var rows=ci.getReturnValue();rows.add(Utils.p("EA_POWER",String.format(Locale.ROOT,"%.0f / %.0f kW",rated,generation)));
        String duration=rated==0?"—":discharge<rated?Lang._t("EA_INSUFFICIENT_DISCHARGE"):String.format(Locale.ROOT,"%.1f s",capacity/rated);
        rows.add(Utils.p("EA_CAPACITY",String.format(Locale.ROOT,"%.0f kJ (%s)",capacity,duration)));
        rows.add(Utils.p("EA_OVERLOAD_DEMAND",String.format(Locale.ROOT,"%.0f / %.0f / %.0f kW",rated+weapons,rated+propulsion,rated+weapons+propulsion)));
    }
}
