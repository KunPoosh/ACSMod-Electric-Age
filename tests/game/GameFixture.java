/* GameFixture.java — 只观察真实绘制边界；切换场景留在输入回调之后。 */
package net.fabricacs.regression.fixtures;
import net.fabricacs.regression.ExternalRuntimeProbe;
import com.zarkonnen.airships.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
public final class GameFixture {
 @Mixin(value=MainMenu.class,remap=false) public abstract static class Menu {
  @Inject(method="render",at=@At("RETURN")) private void rendered(CallbackInfo ci){ExternalRuntimeProbe.menuRendered();}
 }
 @Mixin(value=AirshipGame.class,remap=false) public abstract static class Tick {
  @Inject(method="input",at=@At("RETURN")) private void input(com.zarkonnen.catengine.Input in,CallbackInfo ci){ExternalRuntimeProbe.inputComplete(in);}
 }
 @Mixin(value=UniScreen.class,remap=false) public abstract static class Battle {
  @Inject(method="render",at=@At("RETURN")) private void rendered(CallbackInfo ci){ExternalRuntimeProbe.battleRendered((UniScreen)(Object)this);}
 }
}
