/*
 * ElectricAgeMod.java — 电气时代初始化入口；资源随 MOD 加载，实例模拟由精确注入接入。
 */
package net.poosh.electricage;

import net.fabricacs.api.AcbricInitializer;
import net.fabricacs.api.AcbricModContext;

public final class ElectricAgeMod implements AcbricInitializer {
    public static final String MOD_ID = "electric_age";

    @Override
    public void onInitializeAcbric(AcbricModContext context) {
        net.poosh.electricage.simulation.Balance.DEFAULT.spec("generator");
        context.logger().info("Electric Age / 电气时代 initialized: ship power, overload and chain lightning.");
    }

    @Override
    public void onInitializeAcbric() {
        // 保留 Acbric 无参接口；框架通过带上下文的入口初始化本 MOD。
    }
}
