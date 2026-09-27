/* ModuleMixin.java — 保存、补给和运行倍率；不取消原版模块 tick 的火灾/损坏/维修。 */
package net.poosh.electricage.mixin;

import com.zarkonnen.airships.Airship;
import com.zarkonnen.airships.Combat;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.integration.*;
import net.poosh.electricage.simulation.*;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=Module.class,remap=false)
public abstract class ModuleMixin implements ElectricalModule {
    @Unique private DeviceState electricAge$state;
    @Unique private double electricAge$factor;
    @Unique private double electricAge$reloadRemainder;
    public DeviceState electricAge$state(){return electricAge$state;}
    public void electricAge$state(DeviceState state){electricAge$state=state;}
    public double electricAge$factor(){return electricAge$factor;}
    public void electricAge$factor(double factor){electricAge$factor=factor;}
    @Inject(method="visibleResourceLevel",at=@At("HEAD"),cancellable=true)
    private void electricAge$visualState(CallbackInfoReturnable<Double> ci) {
        Module m=(Module)(Object)this;
        if(ElectricRuntime.key(m)!=null)ci.setReturnValue(net.poosh.electricage.client.ModuleArt.token(m));
    }
    @Inject(method="fire",at=@At("HEAD"),cancellable=true)
    private void electricAge$fire(Combat c,double accuracy,CallbackInfoReturnable<Boolean> ci) {
        Module m=(Module)(Object)this;
        if("lightning".equals(ElectricRuntime.key(m))) ci.setReturnValue(LightningAttack.fire(m,c,accuracy));
    }
    @Inject(method="running",at=@At("RETURN"),cancellable=true)
    private void electricAge$running(CallbackInfoReturnable<Boolean> ci) {
        Module m=(Module)(Object)this;
        if(ci.getReturnValueZ() && ElectricRuntime.key(m)!=null && ElectricRuntime.state(m).spec.kind()==DeviceSpec.Kind.LOAD
                && electricAge$factor<=0) ci.setReturnValue(false);
    }
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/Module;staffProportion()D"))
    private double electricAge$reload(Module m,int ms,Combat c,boolean viewing,double rate,double accuracy,double flammability,double explosion,double command) {
        double original=m.staffProportion();
        if(ElectricRuntime.key(m)==null || ms<=0) return original;
        // 原版随后截为整数工作量；在此保留电气倍率产生的小数，不能每帧丢失。
        double work=original*ms*electricAge$factor+electricAge$reloadRemainder;
        int whole=(int)Math.floor(work); electricAge$reloadRemainder=work-whole;
        return (whole+1e-9)/ms;
    }
    @Inject(method="toJSON",at=@At("RETURN"))
    private void electricAge$save(Combat c,CallbackInfoReturnable<JSONObject> ci) {
        JSONObject data=ElectricRuntime.save((Module)(Object)this);
        if(data!=null) ci.getReturnValue().put(ElectricRuntime.SAVE_KEY,data.put("reloadRemainder",electricAge$reloadRemainder));
    }
    @Inject(method="<init>(Lorg/json/JSONObject;Lcom/zarkonnen/airships/Airship;)V",at=@At("RETURN"))
    private void electricAge$restore(JSONObject o,Airship ship,CallbackInfo ci) {
        JSONObject data=o.optJSONObject(ElectricRuntime.SAVE_KEY);
        ElectricRuntime.restore((Module)(Object)this,data);
        if(data!=null) {
            electricAge$reloadRemainder=data.optDouble("reloadRemainder",0);
            if(!Double.isFinite(electricAge$reloadRemainder)||electricAge$reloadRemainder<0||electricAge$reloadRemainder>=1)
                throw new IllegalArgumentException("Invalid electrical reload remainder");
        }
    }
    @Inject(method="fillUpResources",at=@At("RETURN"),cancellable=true)
    private void electricAge$resupply(CallbackInfoReturnable<Boolean> ci) {
        Module m=(Module)(Object)this;
        DeviceState s=ElectricRuntime.state(m);
        if(s!=null && s.spec.kind()==DeviceSpec.Kind.BATTERY && s.energy<s.spec.capacity()) {
            s.energy=s.spec.capacity();ci.setReturnValue(true);
        }
    }
    @Inject(method="cheapHash",at=@At("RETURN"),cancellable=true)
    private void electricAge$hash(CallbackInfoReturnable<Integer> ci) {
        DeviceState s=ElectricRuntime.state((Module)(Object)this);
        if(s!=null) ci.setReturnValue(31*ci.getReturnValueI()+java.util.Objects.hash(s.id,s.online,s.energy,s.wearRemainder,electricAge$reloadRemainder));
    }
}
