/* RuntimeProbe.java — 在真实 Knot 变换类中验证注入及原版实例，不创建玩家数据。 */
package regression;

import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.integration.*;
import net.poosh.electricage.simulation.*;
import org.json.*;
import java.nio.file.*;
import java.util.*;

public final class RuntimeProbe {
    static int checks;
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS: "+label);}
    static void near(double a,double b,String label){check(Math.abs(a-b)<1e-7,label+" ("+a+")");}
    static ModuleType type(String key,int w,int h,int hp,double propulsion,int lift,int coal) {
        JSONObject o=new JSONObject().put("name","EA_"+key.toUpperCase(Locale.ROOT)).put("w",w).put("h",h)
            .put("appearance",new JSONObject().put("src","ea_probe").put("x",0).put("y",0).put("w",w).put("h",h))
            .put("categories",new JSONArray()).put("hp",hp).put("weight",1).put("cost",1).put("crew",0).put("propulsion",propulsion).put("lift",lift).put("coalReload",coal);
        ModuleType type=new ModuleType(o);Loadable.map.computeIfAbsent(ModuleType.class,k->new HashMap<>()).put(type.name,type);return type;
    }
    public static void main(String[] args)throws Exception {
        check(ElectricalModule.class.isAssignableFrom(Module.class),"Module transformed");
        check(ElectricalShip.class.isAssignableFrom(Airship.class),"Airship transformed");
        Loadable.map.computeIfAbsent(SpritesheetBundle.class,k->new HashMap<>()).put("ea_probe",new SpritesheetBundle(new JSONObject().put("name","ea_probe")));
        // 粒子只是定义构造的默认引用，本探针不绘制或创建粒子。
        var uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
        var smoke=(ParticleType)((sun.misc.Unsafe)uf.get(null)).allocateInstance(ParticleType.class);
        Loadable.map.computeIfAbsent(ParticleType.class,k->new HashMap<>()).put("big_smoke",smoke);
        Airship ship=new Airship(ShipType.AIRSHIP);ship.setName("Electric probe");ship.msSinceOnGround=10000;ship.enginesRunning=true;ship.suspendiumRunning=true;
        Module generator=new Module(ship,type("generator",3,2,240,0,0,10000),0,0);
        Module battery=new Module(ship,type("battery",2,2,160,0,0,0),3,0);
        Module prop=new Module(ship,type("propulsion",2,2,160,.15,0,0),5,0);
        ship.modules.addAll(List.of(generator,battery,prop));
        for(Module m:ship.modules){m.maxHP=m.type.getHp(ship.currentBonuses);m.repairFully();}
        ElectricRuntime.tick(ship,20);
        near(ship.availablePropulsion(false),.15,"native available propulsion receives rated output");
        near(ElectricRuntime.state(battery).energy,2000,"full battery retained");
        generator.msUntilCoal=0;ElectricRuntime.tick(ship,1000);
        near(ElectricRuntime.state(battery).energy,1960,"native fuel qualification uses battery");
        var state=ElectricRuntime.state(battery);state.energy=0;ElectricRuntime.tick(ship,20);
        near(ship.availablePropulsion(false),0,"empty battery disables native output");
        check(prop.canRun(),"native staffing eligibility not tied to power");
        check(!prop.running(),"unpowered operation disabled");
        generator.msUntilCoal=10000;ElectricRuntime.tick(ship,20);near(ship.availablePropulsion(false),.15,"power recovery starts native output");
        ((ElectricalShip)ship).electricAge$request(new PowerGrid.Request(false,true));ElectricRuntime.tick(ship,1000);
        near(ship.availablePropulsion(false),.225,"overload multiplies native propulsion once");check(prop.hp==159,"native doDamage applied integer wear");
        var json=battery.toJSON(null);near(json.getJSONObject("electric_age").getDouble("energy"),state.energy,"module JSON includes actual charge");
        state.energy=123;json=battery.toJSON(null);Module restored=new Module(json,ship);
        near(ElectricRuntime.state(restored).energy,123,"native constructor restores partial charge");
        restored.fillUpResources();near(ElectricRuntime.state(restored).energy,2000,"native resupply fills battery");
        check(restored.cheapHash()!=battery.cheapHash(),"native hash includes stored charge");
        check(!battery.fillUpResources() || ElectricRuntime.state(battery).energy==2000,"resupply reports charge change");
        ((ElectricalShip)ship).electricAge$request(new PowerGrid.Request(false,false));
        battery.hp=0;generator.msUntilCoal=500;
        ElectricRuntime.tick(ship,1000);
        near(((ElectricalShip)ship).electricAge$result().generatedEnergy(),110,"coal expires midway through native frame");
        near(ElectricRuntime.factor(prop),.5,"only powered half-frame contributes propulsion work");
        check(!ElectricRuntime.state(prop).online,"load is offline after fuel boundary");
        near(ElectricRuntime.state(battery).energy,2000,"disabled battery keeps charge");
        battery.hp=battery.getMaxHP();generator.msUntilCoal=10000;
        ship.msSinceOnGround=0;ship.moveTo=new com.zarkonnen.catengine.util.Pt(0,1200);
        ElectricRuntime.tick(ship,20);near(ElectricRuntime.factor(prop),0,"native landed state removes propulsion load");
        ship.msSinceOnGround=10000;ElectricRuntime.tick(ship,20);near(ElectricRuntime.factor(prop),1,"takeoff restores rated load");
        var before=battery.toJSON(null).getJSONObject("electric_age").toString();ElectricRuntime.tick(ship,0);
        check(before.equals(battery.toJSON(null).getJSONObject("electric_age").toString()),"pause preserves electrical state");
        Module copied=new Module(battery.toJSON(null),ship);copied.x=7;ship.modules.add(copied);
        ElectricRuntime.state(battery).energy=321;ElectricRuntime.state(copied).energy=123;
        generator.msUntilCoal=0;prop.hp=0;
        ElectricRuntime.tick(ship,20);
        check(!ElectricRuntime.state(copied).id.equals(ElectricRuntime.state(battery).id),"editor JSON copy receives distinct deterministic identity");
        near(ElectricRuntime.state(battery).energy,321,"identity reconciliation preserves original energy");
        near(ElectricRuntime.state(copied).energy,123,"identity reconciliation preserves copied snapshot energy");
        System.out.println("ELECTRIC RUNTIME PASS: "+checks+" checks");
    }
}
