/* AirshipMixin.java — 每船求解、实例输出缩放、船级请求保存及俘获清理。 */
package net.poosh.electricage.mixin;

import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.integration.*;
import net.poosh.electricage.simulation.PowerGrid;
import org.json.JSONObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value=Airship.class,remap=false)
public abstract class AirshipMixin implements ElectricalShip {
    @Unique private PowerGrid.Request electricAge$request=new PowerGrid.Request(false,false);
    @Unique private PowerGrid.Result electricAge$result;
    @Unique private Module electricAge$currentModule;
    @Unique private net.poosh.electricage.client.PowerSummary electricAge$summary;
    public net.poosh.electricage.client.PowerSummary electricAge$summary(){return electricAge$summary;}
    public void electricAge$summary(net.poosh.electricage.client.PowerSummary s){electricAge$summary=s;}
    public PowerGrid.Request electricAge$request(){
        // 原版有数个独立构造路径；没有扩展字段的旧船也必须具有明确的关闭状态。
        if(electricAge$request==null)electricAge$request=new PowerGrid.Request(false,false);
        return electricAge$request;
    }
    public void electricAge$request(PowerGrid.Request r){electricAge$request=java.util.Objects.requireNonNull(r);}
    public PowerGrid.Result electricAge$result(){return electricAge$result;}
    public void electricAge$result(PowerGrid.Result r){electricAge$result=r;}
    @Redirect(method="splitIfNeeded",at=@At(value="NEW",target="com/zarkonnen/airships/Airship"))
    private Airship electricAge$fragment(ShipType type) {
        Airship fragment=new Airship(type);
        ((ElectricalShip)fragment).electricAge$request(electricAge$request());
        return fragment;
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void electricAge$tick(int ms,Combat combat,boolean won,boolean lost,boolean viewing,
            double command,double fire,double accuracy,double crew,double flammability,double explosion,double cooldown,double repair,double firefight,
            CallbackInfoReturnable<Boolean> ci) {
        if(combat!=null) ElectricRuntime.tick((Airship)(Object)this,ms);
    }
    @Redirect(method={"availablePropulsion","availableLift"},at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/Module;canRun()Z"))
    private boolean electricAge$remember(Module m) { electricAge$currentModule=m;return m.canRun(); }
    @Redirect(method="availablePropulsion",at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/ModuleType;getPropulsion(Lcom/zarkonnen/airships/BonusSet;)D"))
    private double electricAge$propulsion(ModuleType type,BonusSet bonuses) {
        return type.getPropulsion(bonuses)*ElectricRuntime.factor(electricAge$currentModule);
    }
    @Redirect(method="availableLift",at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/ModuleType;getLift(Lcom/zarkonnen/airships/BonusSet;)I"))
    private int electricAge$lift(ModuleType type,BonusSet bonuses) {
        return (int)(type.getLift(bonuses)*DeploymentPreview.liftFactor(electricAge$currentModule));
    }
    @ModifyArgs(method="drawFireArc",at=@At(value="INVOKE",target="Lorg/newdawn/slick/Graphics;drawArc(FFFFFF)V",ordinal=1))
    private void electricAge$rangeCircle(Args args,Module module,com.zarkonnen.catengine.Draw draw,
            double scrollX,double scrollY,double zoom,boolean enabled) {
        if(!"lightning".equals(ElectricRuntime.key(module)))return;
        // 原版 360 度外圈误用 radius-60 作为 Y；只修正本 MOD 闪电，不改变距离判定。
        float diameter=args.get(3);
        args.set(1,(float)((module.fireFrom().y+scrollY)*zoom)-diameter/2);
    }
    @Inject(method="toJSON(Lcom/zarkonnen/airships/Combat;Z)Lorg/json/JSONObject;",at=@At("RETURN"))
    private void electricAge$save(Combat combat,boolean bonuses,CallbackInfoReturnable<JSONObject> ci) {
        var request=electricAge$request();
        if(request.weapons() || request.propulsion())
            ci.getReturnValue().put(ElectricRuntime.SAVE_KEY,new JSONObject().put("version",1).put("weapons",request.weapons()).put("propulsion",request.propulsion()));
    }
    @Inject(method="<init>(Lorg/json/JSONObject;ZLcom/zarkonnen/airships/BonusSet;)V",at=@At("RETURN"))
    private void electricAge$restore(JSONObject o,boolean partial,BonusSet bonuses,CallbackInfo ci) {
        JSONObject data=o.optJSONObject(ElectricRuntime.SAVE_KEY);
        if(data!=null) {
            if(data.getInt("version")!=1) throw new IllegalArgumentException("Unsupported Electric Age ship state version");
            electricAge$request=new PowerGrid.Request(data.getBoolean("weapons"),data.getBoolean("propulsion"));
        }
    }
    @Inject(method="switchSides",at=@At("RETURN"))
    private void electricAge$capture(CallbackInfoReturnable<Boolean> ci) {
        if(ci.getReturnValueZ()) electricAge$request=new PowerGrid.Request(false,false);
    }
    @Inject(method="cheapHash",at=@At("RETURN"),cancellable=true)
    private void electricAge$hash(CallbackInfoReturnable<Integer> ci) {
        var request=electricAge$request();
        if(request.weapons() || request.propulsion())
            ci.setReturnValue(31*ci.getReturnValueI()+request.hashCode());
    }
}
