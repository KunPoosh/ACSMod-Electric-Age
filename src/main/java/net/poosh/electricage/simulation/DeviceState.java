/* DeviceState.java — 模块自身持有电量、启停及损伤余量；换船不重新充电。 */
package net.poosh.electricage.simulation;

public final class DeviceState {
    public final String id;
    public final DeviceSpec spec;
    public boolean eligible = true;
    public boolean online;
    public double energy;
    public double wearRemainder;
    public double maximumHp;
    public double power;
    public double factor;
    public DeviceState(String id, DeviceSpec spec, double initialEnergy, boolean online) {
        if (id == null || id.isBlank() || spec == null) throw new IllegalArgumentException("Missing instance identity");
        this.id=id; this.spec=spec; this.energy=initialEnergy; this.online=online;
        validate();
    }
    public void validate() {
        if (!Double.isFinite(energy) || energy < 0 || energy > spec.capacity()) throw new IllegalArgumentException("Invalid stored energy: " + id);
        if (!Double.isFinite(wearRemainder) || wearRemainder < 0 || wearRemainder >= 1) throw new IllegalArgumentException("Invalid wear remainder: " + id);
        if (!Double.isFinite(maximumHp) || maximumHp < 0) throw new IllegalArgumentException("Invalid maximum HP: " + id);
    }
    public Snapshot snapshot() { return new Snapshot(id,spec.key(),online,energy,wearRemainder); }
    public record Snapshot(String id,String device,boolean online,double energy,double wearRemainder) {
        public DeviceState restore(Balance balance) {
            DeviceState result = new DeviceState(id,balance.spec(device),energy,online);
            result.wearRemainder=wearRemainder; result.validate(); return result;
        }
    }
}
