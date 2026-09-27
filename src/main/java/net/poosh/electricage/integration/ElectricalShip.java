/* ElectricalShip.java — 船级请求与只读供电摘要；请求不等于实际过载。 */
package net.poosh.electricage.integration;

import net.poosh.electricage.simulation.PowerGrid;

public interface ElectricalShip {
    PowerGrid.Request electricAge$request();
    void electricAge$request(PowerGrid.Request request);
    PowerGrid.Result electricAge$result();
    void electricAge$result(PowerGrid.Result result);
    net.poosh.electricage.client.PowerSummary electricAge$summary();
    void electricAge$summary(net.poosh.electricage.client.PowerSummary summary);
}
