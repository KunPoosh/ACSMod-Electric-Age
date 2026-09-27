/* ElectricalModule.java — 注入原版实例的电气状态，不改变共享 ModuleType。 */
package net.poosh.electricage.integration;

import net.poosh.electricage.simulation.DeviceState;

public interface ElectricalModule {
    DeviceState electricAge$state();
    void electricAge$state(DeviceState state);
    double electricAge$factor();
    void electricAge$factor(double factor);
}
