/* LightningAttack.java — 原版选点/散布后立即单点命中，再按能量预算派生表面目标。 */
package net.poosh.electricage.integration;

import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import com.zarkonnen.catengine.util.Pt;
import net.poosh.electricage.mixin.ModuleTargetAccess;
import net.poosh.electricage.simulation.*;
import java.util.*;

public final class LightningAttack {
    public record Bolt(double x1,double y1,double x2,double y2,int energy,int born) {}
    private static final Map<Combat,List<Bolt>> VISUALS=new WeakHashMap<>();
    public static List<Bolt> visuals(Combat c) {
        List<Bolt> bolts=VISUALS.get(c);if(bolts==null)return List.of();
        bolts.removeIf(b->c.time-b.born()>180);return List.copyOf(bolts);
    }
    private static void bolt(Combat c,double x,double y,double tx,double ty,int energy) {
        List<Bolt> bolts=VISUALS.computeIfAbsent(c,k->new ArrayList<>());
        bolts.removeIf(b->c.time-b.born()>180);
        if(bolts.size()>=1024)bolts.removeFirst(); // 只限制表现，不限制攻击结算。
        bolts.add(new Bolt(x,y,tx,ty,energy,c.time));
    }
    public static boolean fire(Module weapon,Combat c,double fleetAccuracy) {
        Pt from=weapon.fireFrom();var access=(ModuleTargetAccess)weapon;
        var target=access.electricAge$target(c,from.x,from.y);if(target==null)return false;
        double distance=StrictMath.hypot(target.b.x-from.x,target.b.y-from.y);
        if(distance>Balance.DEFAULT.value("lightning.range"))return false;
        double mult=1;
        if(weapon.type.getInaccuracyFromWeather()) {
            var effect=c.timeOfDay.effect;mult=effect.shootJitterMult*(target.b.x>from.x?effect.shootToRightJitterMult:effect.shootToLeftJitterMult);
            if(effect.fogClr!=null&&target.b.y>512-effect.fogLevel)mult*=3;
        }
        if(target.a.type==ShipType.AIRSHIP)mult/=weapon.type.accuracyVsAirships(weapon.ship.currentBonuses);
        if(weapon.ship.type==ShipType.AIRSHIP)mult/=weapon.type.accuracyFromAirships(weapon.ship.currentBonuses);
        double x=access.electricAge$jitter(c,target.b.x,distance,false,mult,0,target.a,fleetAccuracy);
        double y=access.electricAge$jitter(c,target.b.y,distance,true,mult,0,target.a,fleetAccuracy);
        weapon.hasPrevJitter=true;weapon.msSinceFired=0;c.msSinceInterestingCombatEvent=0;
        Tile root=tileAt(target.a,x,y);
        if(root==null||root.isMaskedEmpty()||root.module.hp<=0){bolt(c,from.x,from.y,x,y,(int)Balance.DEFAULT.value("lightning.energy"));return true;}
        Map<String,Tile> tiles=new HashMap<>();
        var rootTarget=target(root,c,x,y);tiles.put(rootTarget.id(),root);
        var enemy=c.otherSide(c.sideOf(weapon.ship));
        ChainAttack.World world=new ChainAttack.World() {
            public List<ChainAttack.Target> candidates(ChainAttack.Target origin,double radius) {
                TreeMap<String,ChainAttack.Target> points=new TreeMap<>();
                for(Airship ship:enemy.ships) for(Tile t:ship.tiles) {
                    if(t.module.hp<=0||t.isMaskedEmpty()||!surface(t))continue;
                    double px=ship.getX()+ship.gridXToWorldX(t.x,1)*16+8,py=ship.getY()+t.y*16+8;
                    if(StrictMath.hypot(px-origin.x(),py-origin.y())>radius)continue;
                    var candidate=target(t,c,px,py);var old=points.get(candidate.id());
                    if(old==null || StrictMath.hypot(px-origin.x(),py-origin.y())<StrictMath.hypot(old.x()-origin.x(),old.y()-origin.y())) {
                        points.put(candidate.id(),candidate);tiles.put(candidate.id(),t);
                    }
                }
                return new ArrayList<>(points.values());
            }
            public void pointBlast(ChainAttack.Target point,int damage) {
                Tile t=tiles.get(point.id());if(t==null||t.module.hp<=0)return;
                Shot shot=new PointShot(t.ship,point.x(),point.y(),weapon,from,damage);
                t.ship.hit(shot,c,t.ship.showingOutside,new boolean[]{false});
            }
        };
        var hits=ChainAttack.resolve(rootTarget,(int)Balance.DEFAULT.value("lightning.energy"),(int)Balance.DEFAULT.value("lightning.loss"),
                (int)Balance.DEFAULT.value("lightning.children"),Balance.DEFAULT.value("lightning.radius"),world,c.r::nextInt);
        for(var hit:hits)bolt(c,hit.from()==null?from.x:hit.from().x(),hit.from()==null?from.y:hit.from().y(),hit.target().x(),hit.target().y(),hit.energy());
        return true;
    }
    private static ChainAttack.Target target(Tile t,Combat c,double x,double y) {
        return new ChainAttack.Target(c.sides.indexOf(c.sideOf(t.ship))+":"+c.sideOf(t.ship).ships.indexOf(t.ship)+":"+t.module.x+":"+t.module.y,x,y);
    }
    private static Tile tileAt(Airship s,double x,double y) {
        double lx=x-s.getIntX();if(s.flipped)lx=s.getWidth()*16-lx;
        return s.tileAt((int)StrictMath.floor(lx/16),(int)StrictMath.floor((y-s.getIntY())/16));
    }
    private static boolean surface(Tile t) {
        // 与原版模块边缘判定一致的简单四邻域，不做链段遮挡追踪。
        return t.ship.tileAt(t.x-1,t.y)==null||t.ship.tileAt(t.x+1,t.y)==null||t.ship.tileAt(t.x,t.y-1)==null||t.ship.tileAt(t.x,t.y+1)==null;
    }
    private static final class PointShot extends Shot {
        private final int energy;
        PointShot(Airship target,double x,double y,Module weapon,Pt from,int energy){super(target,x,y,weapon.ship,from.x,from.y,weapon,1);this.energy=energy;}
        @Override public int getBlastDmg(){return energy;}
        @Override public int getPenDmg(){return 0;}
    }
}
