/* OverloadCommands.java — 原生指令分发器内注册目标状态命令；有效变更才扣指令。 */
package net.poosh.electricage.integration;

import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.simulation.*;
import org.json.JSONObject;
import java.lang.reflect.Proxy;
import java.util.*;

public final class OverloadCommands {
    public static final String TYPE="electric_age.overload";
    private OverloadCommands() {}
    @SuppressWarnings({"rawtypes","unchecked"})
    public static void register(Map executors) {
        try {
            // 原版 EXECS 公开但其函数接口为 private；代理保留完整原版记录/执行链。
            Class<?> contract=Class.forName("com.zarkonnen.airships.Combat$CommandExecutor");
            Object executor=Proxy.newProxyInstance(contract.getClassLoader(),new Class<?>[]{contract},(proxy,method,args)-> {
                if(method.getName().equals("run")) { execute((JSONObject)args[0],(Combat)args[1]);return null; }
                return switch(method.getName()) { case "toString"->TYPE;case "hashCode"->System.identityHashCode(proxy);case "equals"->proxy==args[0];default->null; };
            });
            if(executors.putIfAbsent(TYPE,executor)!=null) throw new IllegalStateException("Duplicate electrical command registration");
        } catch(ClassNotFoundException e) { throw new IllegalStateException("Unsupported native command interface",e); }
    }
    public static boolean requested(Airship ship,boolean weapons) {
        var r=((ElectricalShip)ship).electricAge$request();return weapons?r.weapons():r.propulsion();
    }
    public static boolean applicable(Airship ship,boolean weapons) {
        if(requested(ship,weapons)) return true;
        for(Module m:ship.modules) {
            String key=ElectricRuntime.key(m);if(key==null)continue;
            if(Balance.DEFAULT.spec(key).group()==(weapons?DeviceSpec.Group.WEAPONS:DeviceSpec.Group.PROPULSION))return true;
        }
        return false;
    }
    public static boolean apply(Airship ship,boolean weapons,boolean enabled) {
        if(!applicable(ship,weapons)||requested(ship,weapons)==enabled||!ship.readyForCommand())return false;
        var access=(ElectricalShip)ship;var r=access.electricAge$request();
        access.electricAge$request(new PowerGrid.Request(weapons?enabled:r.weapons(),weapons?r.propulsion():enabled));
        ship.commandGiven();return true;
    }
    public static void execute(JSONObject command,Combat combat) {
        Airship ship=combat.getShip(command.getJSONObject("id"));
        if(ship==null || combat.sides.indexOf(combat.sideOf(ship))!=command.getInt("side")
                || ship.multiplayerControllerID!=command.getInt("controller"))return;
        String group=command.getString("group");if(!group.equals("weapons")&&!group.equals("propulsion"))return;
        apply(ship,group.equals("weapons"),command.getBoolean("enabled"));
    }
    public static void send(Combat combat,List<Airship> ships,boolean weapons,boolean enabled) {
        for(Airship ship:ships) if(applicable(ship,weapons)&&requested(ship,weapons)!=enabled&&ship.readyForCommand())
            combat.giveCommand(Client.msg(TYPE).put("id",combat.getShipID(ship)).put("side",combat.sides.indexOf(combat.sideOf(ship)))
                .put("controller",ship.multiplayerControllerID).put("group",weapons?"weapons":"propulsion").put("enabled",enabled));
    }
}
