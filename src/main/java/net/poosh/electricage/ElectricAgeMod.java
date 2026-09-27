/*
 * ElectricAgeMod.java — 电气时代初始化入口；当前仅建立工程，不注册电力玩法或修改游戏状态。
 */
package net.poosh.electricage;

import net.fabricacs.api.AcbricInitializer;
import net.fabricacs.api.AcbricModContext;

public final class ElectricAgeMod implements AcbricInitializer {
    public static final String MOD_ID = "electric_age";

    @Override
    public void onInitializeAcbric(AcbricModContext context) {
        context.logger().info("Electric Age / 电气时代 initialized; project scaffold only.");
    }

    @Override
    public void onInitializeAcbric() {
        // 保留 Acbric 无参接口；框架通过带上下文的入口初始化本 MOD。
    }
}
