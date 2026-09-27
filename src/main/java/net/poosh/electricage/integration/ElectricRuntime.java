/* ElectricRuntime.java — 原版资格与纯电网核心之间的适配，每船模拟步只求解一次。 */
package net.poosh.electricage.integration;

import com.zarkonnen.airships.Airship;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.simulation.*;
import java.util.*;
import org.json.JSONObject;

public final class ElectricRuntime {
    public static final String SAVE_KEY="electric_age";
    private static final PowerGrid GRID=new PowerGrid(Balance.DEFAULT);
    private ElectricRuntime() {}
    public static String key(Module m) {
        if(m.type==null) return null;
        String name=m.type.name;
        if(name.startsWith("FLIPPED_")) name=name.substring(8);
        if(!name.startsWith("EA_"))return null;
        String key=name.substring(3).toLowerCase(Locale.ROOT);
        return switch(key){case "generator","battery","cannon","lightning","propulsion","lift"->key;default->null;};
    }
    public static DeviceState state(Module m) {
        String key=key(m); if(key==null) return null;
        ElectricalModule access=(ElectricalModule)m;
        DeviceState s=access.electricAge$state();
        if(s==null) {
            DeviceSpec spec=Balance.DEFAULT.spec(key);
            s=new DeviceState(m.type.name+":"+m.x+":"+m.y,spec,spec.capacity(),false);
            access.electricAge$state(s);
        }
        return s;
    }
    public static double factor(Module m) {
        return key(m)==null ? 1 : ((ElectricalModule)m).electricAge$factor();
    }
    public static void tick(Airship ship,int milliseconds) {
        if(milliseconds<=0) return;
        List<DeviceState> states=new ArrayList<>();List<Module> modules=new ArrayList<>();Set<String> identities=new HashSet<>();
        ElectricalShip grid=(ElectricalShip)ship;
        for(Module m:ship.modules) {
            DeviceState s=state(m); if(s==null) continue;
            if(!identities.add(s.id)) {
                // 编辑器复制/移动可复制扩展 JSON。只消歧身份，不复制或重置电量；
                // 原版模块列表顺序随船序列化，所有模拟端采用相同的消歧顺序。
                String id;int suffix=0;
                do{id=s.id+"@"+m.x+","+m.y+"#"+(suffix++);}while(!identities.add(id));
                s=new DeviceState.Snapshot(id,s.spec.key(),s.online,s.energy,s.wearRemainder).restore(Balance.DEFAULT);
                ((ElectricalModule)m).electricAge$state(s);
            }
            // canRun 保持原版原义，人员分配不依赖电气在线，避免缺人/缺电恢复死锁。
            s.eligible=m.hp>0 && m.canRun();
            if(s.spec.group()==DeviceSpec.Group.PROPULSION && ship.grounded()) s.eligible=false;
            s.maximumHp=m.getMaxHP();
            states.add(s);modules.add(m);
        }
        if(states.isEmpty()) { grid.electricAge$result(null);grid.electricAge$summary(null);return; }
        // 原版在稍后的 Module.tick 扣燃料；先按本步内的煤炭耗尽边界结算，不能多发一整帧。
        TreeSet<Integer> boundaries=new TreeSet<>();boundaries.add(0);boundaries.add(milliseconds);
        for(Module m:modules) if(state(m).spec.kind()==DeviceSpec.Kind.GENERATOR && m.type.getCoalReload(ship.currentBonuses)>0
                && m.msUntilCoal>0 && m.msUntilCoal<milliseconds) boundaries.add(m.msUntilCoal);
        Map<String,PowerGrid.Work> totals=new LinkedHashMap<>();
        double generated=0,used=0,spilled=0;PowerGrid.Result result=null;int from=0;
        for(int to:boundaries) {
            if(to==0)continue;
            for(Module m:modules) if(state(m).spec.kind()==DeviceSpec.Kind.GENERATOR && m.type.getCoalReload(ship.currentBonuses)>0
                    && m.msUntilCoal<=from)state(m).eligible=false;
            result=GRID.advance(states,grid.electricAge$request(),(to-from)/1000.0);
            generated+=result.generatedEnergy();used+=result.usedEnergy();spilled+=result.spilledEnergy();
            result.work().forEach((id,w)->totals.merge(id,w,(a,b)->new PowerGrid.Work(a.factorSeconds()+b.factorSeconds(),
                    a.poweredSeconds()+b.poweredSeconds(),a.overloadSeconds()+b.overloadSeconds(),a.damage()+b.damage())));
            from=to;
        }
        result=new PowerGrid.Result(result.generation(),result.demand(),result.consumption(),result.overloadFraction(),generated,used,spilled,Collections.unmodifiableMap(totals));
        grid.electricAge$result(result);
        grid.electricAge$summary(net.poosh.electricage.client.PowerSummary.of(states,result));
        for(Module m:modules) {
            DeviceState s=state(m);PowerGrid.Work work=result.work().get(s.id);
            double factor=s.spec.kind()==DeviceSpec.Kind.LOAD ? work.factorSeconds()/(milliseconds/1000.0) : (s.eligible ? 1:0);
            ((ElectricalModule)m).electricAge$factor(factor);
            if(work.damage()>0) m.doDamage(work.damage());
        }
    }
    public static JSONObject save(Module m) {
        DeviceState s=state(m); if(s==null) return null;
        return new JSONObject().put("version",1).put("id",s.id).put("online",s.online)
                .put("energy",s.energy).put("wear",s.wearRemainder);
    }
    public static void restore(Module m,JSONObject data) {
        if(key(m)==null || data==null) return;
        if(data.getInt("version")!=1) throw new IllegalArgumentException("Unsupported Electric Age module state version");
        DeviceState s=new DeviceState.Snapshot(data.getString("id"),key(m),data.getBoolean("online"),data.getDouble("energy"),data.getDouble("wear")).restore(Balance.DEFAULT);
        ((ElectricalModule)m).electricAge$state(s);
    }
}
