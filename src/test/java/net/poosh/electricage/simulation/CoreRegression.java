/* CoreRegression.java — 独立于游戏的电力、损伤、持久化及闪电预算边界回归。 */
package net.poosh.electricage.simulation;

import java.util.*;
import static net.poosh.electricage.simulation.DeviceSpec.Kind.*;
import static net.poosh.electricage.simulation.DeviceSpec.Group.*;

public final class CoreRegression {
    static final Balance B=Balance.DEFAULT;
    static final PowerGrid GRID=new PowerGrid(B);
    static final PowerGrid.Request OFF=new PowerGrid.Request(false,false), BOTH=new PowerGrid.Request(true,true);
    static int checks;
    static void check(boolean condition,String name) { if(!condition) throw new AssertionError(name); checks++; }
    static void near(double actual,double expected,String name) { check(Math.abs(actual-expected)<1e-7,name+": "+actual+" != "+expected); }
    static void rejects(Runnable fn,String name) { try { fn.run(); } catch(IllegalArgumentException expected) { checks++; return; } throw new AssertionError(name); }
    static DeviceState d(String key) { return new DeviceState(key,B.spec(key),B.spec(key).capacity(),false); }
    static List<DeviceState> fixture() { return new ArrayList<>(List.of(d("generator"),d("battery"),d("cannon"),d("lightning"),d("propulsion"),d("lift"))); }
    static DeviceState gen(double watts) { return new DeviceState("source",new DeviceSpec("test",GENERATOR,NONE,0,0,0,0,watts,0,0,0),0,false); }
    static DeviceState load(String id,double rated,double minimum,boolean online) {
        return new DeviceState(id,new DeviceSpec(id,LOAD,NONE,rated,minimum,rated,0,0,0,0,0),0,online);
    }
    static double energy(List<DeviceState> ds) { return ds.stream().mapToDouble(s->s.energy).sum(); }
    static void standard() {
        var ds=fixture(); ds.get(1).energy=0;
        var r=GRID.advance(ds,OFF,10); near(ds.get(1).energy,200,"20 kW charging"); near(r.usedEnergy(),2000,"rated consumption");
        ds=fixture(); r=GRID.advance(ds,BOTH,25); near(ds.get(1).energy,0,"25 s dual overload"); near(r.usedEnergy(),7500,"full overload energy");
        near(ds.get(2).power,66,"generator surplus continues partial overload"); near(ds.get(2).factor,1.084,"smooth partial curve");
        ds=fixture(); ds.removeFirst(); r=GRID.advance(ds,OFF,12); near(energy(ds),0,"battery exhausted"); near(r.usedEnergy(),2000,"no power after empty boundary");
        near(r.work().get("cannon").poweredSeconds(),10,"exact battery support time"); check(!ds.get(1).online,"depletion shutdown");
        ds=fixture(); ds.get(1).eligible=false; ds.get(0).eligible=false;
        GRID.advance(ds,OFF,1); near(ds.get(1).energy,2000,"disabled battery retains charge"); check(!ds.get(2).online,"disabled battery not accessible");
        ds.get(1).eligible=true; GRID.advance(ds,OFF,1); near(ds.get(1).energy,1800,"repaired battery accessible");
        ds=fixture(); var before=ds.stream().map(DeviceState::snapshot).toList(); GRID.advance(ds,BOTH,0);
        check(before.equals(ds.stream().map(DeviceState::snapshot).toList()),"pause preserves state");
        ds=fixture(); ds.get(1).energy=1999; ds.subList(2,6).clear(); r=GRID.advance(ds,OFF,1);
        near(energy(ds),2000,"charge stops at full"); near(r.spilledEnergy(),219,"unused generation counted once");
    }
    static void shedding() {
        var a=load("a",100,80,true);var b=load("b",200,100,true);var c=load("c",100,30,true);
        GRID.advance(List.of(gen(280),a,b,c),OFF,1); check(!a.online && b.online && c.online,"highest ratio shed alone"); near(b.power,280*2.0/3,"proportional redistribution");
        GRID.advance(List.of(gen(350),a,b,c),OFF,1); check(!a.online,"no partial restart");
        GRID.advance(List.of(gen(400),a,b,c),OFF,1); check(a.online,"rated surplus restarts");
        var small=load("small",40,24,true);var big=load("big",60,36,true);
        GRID.advance(List.of(gen(50),big,small),OFF,1); check(small.online && !big.online,"same threshold large first off");
        var exact=load("exact",100,60,true); GRID.advance(List.of(gen(60),exact),OFF,1); check(exact.online,"equal minimum stays on");
        exact.online=false;GRID.advance(List.of(gen(60),exact),OFF,1);check(!exact.online,"hysteresis at minimum");
        var low=load("low",40,10,false);var high=load("high",60,15,false);
        GRID.advance(List.of(gen(70),high,low),OFF,1);check(low.online&&!high.online,"small rated restarts first");
        low.eligible=false;GRID.advance(List.of(gen(70),high,low),OFF,1);check(!low.online&&high.online,"ineligible load releases demand");
    }
    static void wearAndRestore() {
        var ds=fixture();ds.get(2).maximumHp=180;
        int damage=0; for(int i=0;i<100;i++) damage+=GRID.advance(ds,BOTH,.02).work().get("cannon").damage();
        near(damage,3,"integer wear accumulated");near(ds.get(2).wearRemainder,.6,"fractional wear retained");
        var restored=ds.stream().map(x->x.snapshot().restore(B)).toList();restored.get(2).maximumHp=180;
        var r1=GRID.advance(ds,BOTH,.5);var r2=GRID.advance(restored,BOTH,.5);
        check(ds.stream().map(DeviceState::snapshot).toList().equals(restored.stream().map(DeviceState::snapshot).toList()),"restore continues exact state");
        check(r1.work().equals(r2.work()),"restore continues work and damage");
        var d=ds.get(2); double remainder=d.wearRemainder;
        GRID.advance(ds,OFF,.5);near(d.wearRemainder,remainder,"no wear at rated");
        var bat=d("battery");bat.energy=700;GRID.advance(List.of(bat),OFF,1);near(bat.energy,700,"isolated split no free charge");
    }
    static void properties() {
        Random random=new Random(20260927);
        for(int n=0;n<500;n++) {
            var ds=fixture();ds.get(0).eligible=random.nextBoolean();ds.get(1).energy=random.nextDouble()*2000;
            for(int i=2;i<6;i++) { ds.get(i).online=random.nextBoolean();ds.get(i).eligible=random.nextBoolean(); }
            var copy=ds.stream().map(d->{var v=d.snapshot().restore(B);v.eligible=d.eligible;return v;}).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            Collections.shuffle(copy,random);
            var req=new PowerGrid.Request(random.nextBoolean(),random.nextBoolean());double seconds=random.nextDouble()*20;
            double initial=energy(ds);var r=GRID.advance(ds,req,seconds);var other=GRID.advance(copy,req,seconds);
            near(initial+r.generatedEnergy()-r.usedEnergy()-r.spilledEnergy(),energy(ds),"energy conserved scenario "+n);
            check(r.work().equals(other.work()),"input iteration independent "+n);
            for(var d:ds) { check(d.energy>=0&&d.energy<=d.spec.capacity(),"bounded energy");
                if(d.spec.kind()==LOAD) check(d.online ? d.power+1e-8>=d.spec.minimum()&&d.power<=d.spec.overload()+1e-8 : d.power==0,"valid terminal allocation"); }
        }
        rejects(()->GRID.advance(List.of(d("battery"),d("battery")),OFF,1),"duplicate identity rejected");
        rejects(()->GRID.advance(List.of(),OFF,Double.NaN),"NaN time rejected");
        rejects(()->new DeviceState("bad",B.spec("battery"),2001,false),"overcapacity state rejected");
    }
    static void chains() {
        var targets=new ArrayList<ChainAttack.Target>();for(int i=0;i<20;i++) targets.add(new ChainAttack.Target("t"+i,i,0));
        class World implements ChainAttack.World {
            int damage; public List<ChainAttack.Target> candidates(ChainAttack.Target origin,double radius) {return targets;}
            public void pointBlast(ChainAttack.Target t,int amount) {damage+=amount;}
        }
        var world=new World();var hits=ChainAttack.resolve(targets.getFirst(),100,20,1,96,world,bound->0);
        check(hits.stream().map(ChainAttack.Hit::energy).toList().equals(List.of(100,80,60,40,20)),"serial energy sequence");near(world.damage,300,"serial total damage");
        world=new World();hits=ChainAttack.resolve(targets.getFirst(),100,20,2,96,world,bound->0);
        check(hits.stream().map(ChainAttack.Hit::energy).toList().equals(List.of(100,40,40,20,20)),"branching energy sequence");near(world.damage,220,"branch total damage");
        check(hits.stream().map(h->h.target().id()).distinct().count()==hits.size(),"shared global visited set");
        Collections.reverse(targets);var replay=ChainAttack.resolve(new ChainAttack.Target("t0",0,0),100,20,2,96,new World(),bound->0);
        check(hits.equals(replay),"candidate ordering independent");
        var remote=new ChainAttack.Target("far",1000,0);targets.clear();targets.add(remote);
        check(ChainAttack.resolve(new ChainAttack.Target("root",0,0),100,20,2,96,new World(),bound->0).size()==1,"radius enforced");
        rejects(()->ChainAttack.resolve(remote,100,0,2,96,new World(),bound->0),"zero loss rejected");
    }
    public static void main(String[] args) {standard();shedding();wearAndRestore();properties();chains();System.out.println("Electric Age core: "+checks+" checks passed");}
}
