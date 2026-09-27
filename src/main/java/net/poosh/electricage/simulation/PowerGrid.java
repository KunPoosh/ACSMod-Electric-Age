/* PowerGrid.java — 按稳定顺序结算全船供电；电池空/满边界分段，保持能量守恒。 */
package net.poosh.electricage.simulation;

import java.util.*;
import static net.poosh.electricage.simulation.DeviceSpec.Kind.*;
import static net.poosh.electricage.simulation.DeviceSpec.Group.*;

public final class PowerGrid {
    private static final double EPS = 1e-9;
    private final Balance balance;
    public PowerGrid(Balance balance) { this.balance=Objects.requireNonNull(balance); }
    public record Request(boolean weapons, boolean propulsion) {
        boolean includes(DeviceSpec s) { return s.group()==WEAPONS ? weapons : s.group()==PROPULSION && propulsion; }
    }
    public record Work(double factorSeconds, double poweredSeconds, double overloadSeconds, int damage) {}
    public record Result(double generation, double demand, double consumption, double overloadFraction,
                         double generatedEnergy, double usedEnergy, double spilledEnergy, Map<String,Work> work) {}
    private record Allocation(double generation,double demand,double consumption,double fraction,Map<DeviceState,Double> batteryRates) {}

    public Result advance(Collection<DeviceState> devices, Request request, double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) throw new IllegalArgumentException("Invalid simulation duration");
        Objects.requireNonNull(request);
        List<DeviceState> ordered=new ArrayList<>(devices);
        ordered.sort(Comparator.comparing(s->s.id));
        Set<String> ids=new HashSet<>();
        for (DeviceState d:ordered) { d.validate(); if (!ids.add(d.id)) throw new IllegalArgumentException("Duplicate instance: "+d.id); }
        Map<String,double[]> work=new LinkedHashMap<>();
        for(DeviceState d:ordered) work.put(d.id,new double[4]);
        // 暂停不改变启停、电量、余量或上一次绘制快照。
        if(seconds==0) return new Result(0,0,0,0,0,0,0,Map.of());
        double remaining=seconds,generated=0,used=0,spilled=0;
        Allocation a=null;
        while (remaining>0) {
            a=allocate(ordered,request);
            double dt=remaining;
            for(var e:a.batteryRates.entrySet()) {
                DeviceState d=e.getKey(); double rate=e.getValue();
                if(rate<0) dt=Math.min(dt,d.energy/-rate);
                else if(rate>0) dt=Math.min(dt,(d.spec.capacity()-d.energy)/rate);
            }
            if (!(dt>0)) throw new IllegalStateException("Non-progressing battery boundary");
            double stored=0;
            for(var e:a.batteryRates.entrySet()) {
                DeviceState d=e.getKey(); double delta=e.getValue()*dt;
                d.energy=Math.max(0,Math.min(d.spec.capacity(),d.energy+delta)); stored+=delta;
                if(d.energy<EPS) d.energy=0;
                if(d.spec.capacity()-d.energy<EPS) d.energy=d.spec.capacity();
            }
            generated+=a.generation*dt; used+=a.consumption*dt;
            spilled+=Math.max(0,(a.generation-a.consumption)*dt-stored);
            for(DeviceState d:ordered) if(d.spec.kind()==LOAD && d.online) {
                double[] w=work.get(d.id); w[0]+=d.factor*dt; w[1]+=dt;
                if(d.power>d.spec.rated()+EPS) {
                    w[2]+=dt;
                    double damage=d.wearRemainder+d.maximumHp*balance.wear*dt;
                    int whole=(int)Math.floor(damage+1e-12);
                    d.wearRemainder=Math.max(0,damage-whole); w[3]+=whole;
                }
            }
            remaining=Math.max(0,remaining-dt);
            if(remaining<EPS) remaining=0;
        }
        // 边界处发布当前可用供电，而非上一段耗尽前的快照。
        a=allocate(ordered,request);
        Map<String,Work> result=new LinkedHashMap<>();
        work.forEach((id,w)->result.put(id,new Work(w[0],w[1],w[2],(int)w[3])));
        return new Result(a.generation,a.demand,a.consumption,a.fraction,generated,used,spilled,Collections.unmodifiableMap(result));
    }

    private Allocation allocate(List<DeviceState> devices,Request request) {
        double generation=0,discharge=0,demand=0;
        List<DeviceState> loads=new ArrayList<>(),batteries=new ArrayList<>();
        for(DeviceState d:devices) {
            d.power=0; d.factor=0;
            if(!d.eligible) { d.online=false; continue; }
            switch(d.spec.kind()) {
                case GENERATOR -> { generation+=d.spec.generation(); d.power=d.spec.generation(); d.factor=1; }
                case BATTERY -> { batteries.add(d); if(d.energy>0) discharge+=d.spec.discharge(); }
                case LOAD -> { loads.add(d); demand+=d.spec.rated(); }
            }
        }
        double available=generation+discharge;
        Comparator<DeviceState> startup=Comparator.comparingDouble((DeviceState d)->d.spec.rated()).thenComparing(d->d.id);
        // 最低比例高者先断；比例相同时保留小额定设备；实例键最后打破平局。
        Comparator<DeviceState> shedding=Comparator.comparingDouble((DeviceState d)->d.spec.minimum()/d.spec.rated()).reversed()
                .thenComparing(startup.reversed());
        double rated=loads.stream().filter(d->d.online).mapToDouble(d->d.spec.rated()).sum();
        List<DeviceState> shedOrder=new ArrayList<>(loads); shedOrder.sort(shedding);
        for(DeviceState d:shedOrder) if(d.online && available+EPS<rated && available/rated+1e-12<d.spec.minimum()/d.spec.rated()) {
            d.online=false; rated-=d.spec.rated();
        }
        loads.sort(startup);
        if(available+EPS>=rated) for(DeviceState d:loads) if(!d.online && available-rated+EPS>=d.spec.rated()) {
            d.online=true; rated+=d.spec.rated();
        }
        double ratio=rated==0 ? 0 : Math.min(1,available/rated);
        double extra=loads.stream().filter(d->d.online && request.includes(d.spec)).mapToDouble(d->d.spec.overload()-d.spec.rated()).sum();
        double u=extra==0 ? 0 : Math.max(0,Math.min(1,(available-rated)/extra));
        double consumption=0;
        for(DeviceState d:loads) if(d.online) {
            double du=request.includes(d.spec) ? u : 0;
            d.power=d.spec.rated()*ratio+du*(d.spec.overload()-d.spec.rated());
            d.factor=ratio<1 ? ratio : 1+d.spec.bonus()*(balance.linear*du+balance.quadratic*du*du);
            consumption+=d.power;
        }
        Map<DeviceState,Double> rates=new LinkedHashMap<>();
        double deficit=consumption-generation;
        double cap=batteries.stream().filter(d->deficit>EPS ? d.energy>0 : d.energy<d.spec.capacity())
                .mapToDouble(d->deficit>EPS ? d.spec.discharge() : d.spec.charge()).sum();
        double fraction=cap==0 ? 0 : Math.min(1,Math.abs(deficit)/cap);
        for(DeviceState d:batteries) {
            double rate=0;
            if(deficit>EPS && d.energy>0) rate=-d.spec.discharge()*fraction;
            else if(deficit < -EPS && d.energy<d.spec.capacity()) rate=d.spec.charge()*fraction;
            rates.put(d,rate); d.power=rate;
        }
        return new Allocation(generation,demand,consumption,u,rates);
    }
}
