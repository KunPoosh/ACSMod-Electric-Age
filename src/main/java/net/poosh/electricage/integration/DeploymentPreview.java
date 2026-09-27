/* DeploymentPreview.java — 部署查询按额定电浮力预览，不推进电网、不消耗资源。 */
package net.poosh.electricage.integration;

import com.zarkonnen.airships.Airship;
import com.zarkonnen.airships.Module;

public final class DeploymentPreview {
    private static final ThreadLocal<Airship> ACTIVE=new ThreadLocal<>();
    private DeploymentPreview(){}
    public static boolean hasElectricLift(Airship ship) {
        return ship.modules.stream().anyMatch(m->"lift".equals(ElectricRuntime.key(m)));
    }
    public static int ceiling(Airship ship) {
        Airship previous=ACTIVE.get();
        ACTIVE.set(ship);
        try { return ship.availableServiceCeiling(null); }
        finally { if(previous==null)ACTIVE.remove();else ACTIVE.set(previous); }
    }
    public static double liftFactor(Module module) {
        // 仅在明确的部署调用范围内替换电浮力倍率，保留原版损伤/人员/开关资格。
        if(module!=null && ACTIVE.get()==module.ship && "lift".equals(ElectricRuntime.key(module)))return 1;
        return ElectricRuntime.factor(module);
    }
}
