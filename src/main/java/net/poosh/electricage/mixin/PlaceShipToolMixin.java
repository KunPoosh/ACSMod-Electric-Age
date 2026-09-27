/* PlaceShipToolMixin.java — 部署升限线和高度限制共用只读额定电浮力预览。 */
package net.poosh.electricage.mixin;

import com.zarkonnen.airships.*;
import net.poosh.electricage.integration.DeploymentPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=PlaceShipTool.class,remap=false)
public abstract class PlaceShipToolMixin {
    @Redirect(method="getPlacement(Lcom/zarkonnen/airships/Airship;DDZLcom/zarkonnen/airships/LandFormation;Lcom/zarkonnen/airships/ShipList;ZI)Lcom/zarkonnen/airships/PlaceShipTool$Placement;",
        at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/Airship;availableServiceCeiling(Lcom/zarkonnen/airships/Combat;)I"))
    private static int electricAge$placement(Airship ship,Combat combat) {
        return DeploymentPreview.hasElectricLift(ship)?DeploymentPreview.ceiling(ship):ship.availableServiceCeiling(combat);
    }
    @Redirect(method="draw",at=@At(value="INVOKE",target="Lcom/zarkonnen/airships/Airship;serviceCeiling()I"))
    private int electricAge$guide(Airship ship) {
        return DeploymentPreview.hasElectricLift(ship)?DeploymentPreview.ceiling(ship):ship.serviceCeiling();
    }
}
