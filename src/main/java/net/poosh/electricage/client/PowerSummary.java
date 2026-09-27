/* PowerSummary.java — 模拟阶段生成界面摘要；绘制不遍历模块或推进供电。 */
package net.poosh.electricage.client;
import net.poosh.electricage.simulation.*;
import java.util.List;
public record PowerSummary(double generation,double available,double used,double unmet,double offline,double stored,double capacity,double batteryRate) {
    public static PowerSummary of(List<DeviceState> states,PowerGrid.Result result) {
        double unmet=0,offline=0,stored=0,capacity=0,discharge=0,batteryRate=0;
        for(var s:states) {
            if(s.spec.kind()==DeviceSpec.Kind.BATTERY){stored+=s.energy;capacity+=s.spec.capacity();batteryRate+=s.power;if(s.eligible&&s.energy>0)discharge+=s.spec.discharge();}
            if(s.spec.kind()==DeviceSpec.Kind.LOAD&&s.eligible) {
                if(s.online)unmet+=Math.max(0,s.spec.rated()-s.power);else offline+=s.spec.rated();
            }
        }
        return new PowerSummary(result.generation(),result.generation()+discharge,result.consumption(),unmet,offline,stored,capacity,batteryRate);
    }
}
