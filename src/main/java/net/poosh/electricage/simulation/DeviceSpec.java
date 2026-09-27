/* DeviceSpec.java — 与游戏类无关的设备定义及严格数值校验。 */
package net.poosh.electricage.simulation;

public record DeviceSpec(String key, Kind kind, Group group, double rated, double minimum,
                         double overload, double bonus, double generation, double capacity,
                         double discharge, double charge) {
    public enum Kind { GENERATOR, BATTERY, LOAD }
    public enum Group { NONE, WEAPONS, PROPULSION }
    public DeviceSpec {
        if (key == null || key.isBlank() || kind == null || group == null) throw new IllegalArgumentException("Missing device identity");
        for (double v : new double[]{rated, minimum, overload, bonus, generation, capacity, discharge, charge})
            if (!Double.isFinite(v) || v < 0) throw new IllegalArgumentException("Invalid device value: " + key);
        if (kind == Kind.LOAD && (rated <= 0 || minimum <= 0 || minimum > rated || overload < rated))
            throw new IllegalArgumentException("Invalid load thresholds: " + key);
        if (kind != Kind.LOAD && (rated != 0 || minimum != 0 || overload != 0 || bonus != 0 || group != Group.NONE))
            throw new IllegalArgumentException("Non-load has load fields: " + key);
        if (kind != Kind.BATTERY && (capacity != 0 || discharge != 0 || charge != 0)) throw new IllegalArgumentException("Non-battery storage");
        if (kind == Kind.BATTERY && (capacity <= 0 || discharge <= 0 || charge <= 0)) throw new IllegalArgumentException("Invalid battery");
        if (kind != Kind.GENERATOR && generation != 0) throw new IllegalArgumentException("Non-generator output");
    }
}
