/* ChainAttack.java — 一次完成链式攻击；候选/命中由原版适配，视觉不参与随机。 */
package net.poosh.electricage.simulation;

import java.util.*;
import java.util.function.IntUnaryOperator;

public final class ChainAttack {
    public record Target(String id, double x,double y) {
        public Target { if(id==null || !Double.isFinite(x) || !Double.isFinite(y)) throw new IllegalArgumentException("Invalid chain target"); }
    }
    public record Hit(Target from,Target target,int energy) {}
    public interface World {
        // 只返回当前存活敌方表面模块，每个模块一个稳定标识。
        List<Target> candidates(Target origin,double radius);
        void pointBlast(Target target,int damage);
    }
    private record Pending(Target from,Target target,int energy) {}
    public static List<Hit> resolve(Target root,int energy,int loss,int maxChildren,double radius,
                                    World world,IntUnaryOperator randomIndex) {
        Objects.requireNonNull(root); Objects.requireNonNull(world); Objects.requireNonNull(randomIndex);
        if(loss<=0 || energy<loss || energy>1_000_000 || maxChildren<1 || maxChildren>64 || !Double.isFinite(radius) || radius<=0)
            throw new IllegalArgumentException("Invalid chain budget");
        // 正常预算的节点数 <= E0/C；使用迭代队列，不用递归深度截断攻击。
        if(energy/loss>4096) throw new IllegalArgumentException("Chain budget exceeds technical capacity");
        Set<String> visited=new HashSet<>(); visited.add(root.id);
        ArrayDeque<Pending> queue=new ArrayDeque<>(); queue.add(new Pending(null,root,energy));
        List<Hit> hits=new ArrayList<>();
        while(!queue.isEmpty()) {
            Pending p=queue.removeFirst(); world.pointBlast(p.target,p.energy);
            hits.add(new Hit(p.from,p.target,p.energy));
            int rest=p.energy-loss;
            if(rest<loss) continue;
            TreeMap<String,Target> unique=new TreeMap<>();
            for(Target t:world.candidates(p.target,radius)) if(!visited.contains(t.id)
                    && Math.hypot(t.x-p.target.x,t.y-p.target.y)<=radius) unique.putIfAbsent(t.id,t);
            List<Target> candidates=new ArrayList<>(unique.values());
            int count=Math.min(Math.min(maxChildren,candidates.size()),rest/loss);
            for(int i=0;i<count;i++) {
                int index=randomIndex.applyAsInt(candidates.size());
                if(index<0 || index>=candidates.size()) throw new IllegalArgumentException("Invalid simulation random index");
                Target target=candidates.remove(index); visited.add(target.id);
                // 整数能量平分，余数依确定性的抽取顺序分配，绝不凭空增加预算。
                queue.addLast(new Pending(p.target,target,rest/count+(i<rest%count ? 1:0)));
            }
        }
        return List.copyOf(hits);
    }
}
