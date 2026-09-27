/* ElectricUi.java — 复用原版按钮与绘制接口，所有点击通过原版命令通路。 */
package net.poosh.electricage.client;
import com.zarkonnen.airships.*;
import com.zarkonnen.catengine.*;
import com.zarkonnen.catengine.util.*;
import net.fabricacs.api.util.AcbricLanguage;
import net.poosh.electricage.integration.*;
import java.util.*;

public final class ElectricUi {
    private static final Clr GREEN=new Clr(53,146,92),YELLOW=new Clr(234,191,58),RED=new Clr(212,64,55),GRAY=new Clr(107,113,120);
    private static final OverloadButton WEAPONS=new OverloadButton(true),PROPULSION=new OverloadButton(false);
    private ElectricUi(){}
    public static void install(UniScreen us) {
        us.upperVisualLayers.add(new Bolts());
    }
    private static List<Airship> controlled(UniScreen us) {
        if(!(us.intent instanceof CombatIntent intent)||us.intent instanceof PlaybackIntent||intent.spectate()||us.combat==null)return List.of();
        Airship direct=DirectControlPanel.getShip(us);
        List<Airship> selected=direct!=null?List.of(direct):us.selectedShip==null?us.selectedShips:List.of(us.selectedShip);
        return selected.stream().filter(s->intent.isShipPlayerControlled(us,s)).toList();
    }
    public static final class OverloadButton extends UniScreen.Button {
        private final boolean weapons;
        private final Img icon;
        public OverloadButton(boolean weapons){this.weapons=weapons;icon=new Img("ui",weapons?112:224,416,16,16,false);}
        private List<Airship> ships(UniScreen us){return controlled(us).stream().filter(s->OverloadCommands.applicable(s,weapons)).toList();}
        private boolean target(List<Airship> ships){return ships.stream().noneMatch(s->OverloadCommands.requested(s,weapons));}
        public boolean visible(UniScreen us){return !ships(us).isEmpty();}
        public boolean enabled(UniScreen us){var ships=ships(us);boolean target=target(ships);return ships.stream().anyMatch(s->s.readyForCommand()&&OverloadCommands.requested(s,weapons)!=target);}
        public boolean isToggle(){return true;}
        public boolean selected(UniScreen us){return !target(ships(us));}
        public Img icon(UniScreen us){return icon;}
        public String text(UniScreen us){return weapons?AcbricLanguage.text("Weapon overload","武器过载"):AcbricLanguage.text("Propulsion overload","动力过载");}
        public String tooltip(UniScreen us){return text(us)+" — "+(target(ships(us))?AcbricLanguage.text("Enable","开启"):AcbricLanguage.text("Disable","关闭"))+"\n"+AcbricLanguage.text("Each ready ship spends one command. Actual overload damages devices.","每艘就绪舰船消耗一次指令。实际过载会损伤模块。");}
        public void click(Input in,UniScreen us){var ships=ships(us);OverloadCommands.send(us.combat,ships,weapons,target(ships));}
        public void renderExtra(UniScreen us,MyDraw d,int x,int y,int w,int h){var ships=ships(us);long on=ships.stream().filter(s->OverloadCommands.requested(s,weapons)).count();if(on>0&&on<ships.size())d.rect(YELLOW,x+3,y+h-5,w-6,2);}
    }
    public static int overloadRowHeight(UniScreen us) {
        if(us.hideUI || us.combat==null || us.combat.startCountdown>0)return 0;
        return WEAPONS.visible(us)||PROPULSION.visible(us)?MyDraw.BUTTON_H+MyDraw.BUTTON_SPACING:0;
    }
    public static int panelExtraHeight(UniScreen us) {
        return resourceRowHeight(us)+overloadRowHeight(us);
    }
    public static void overloadRow(MyDraw d,UniScreen us,int x,int y,int width) {
        if(overloadRowHeight(us)==0)return;
        int buttonWidth=(width-MyDraw.BUTTON_SPACING)/2;
        for(OverloadButton button:List.of(WEAPONS,PROPULSION)) {
            // 保留两个固定位置，无对应设备的一侧显示不可用，避免按钮随损毁左右跳动。
            var ships=button.ships(us);
            long on=ships.stream().filter(s->OverloadCommands.requested(s,button.weapons)).count();
            String state=ships.isEmpty()?AcbricLanguage.text("N/A","无"):
                on==0?AcbricLanguage.text("Off","关"):on==ships.size()?AcbricLanguage.text("On","开"):AcbricLanguage.text("Mixed","部分");
            d.toggle(x,y,buttonWidth,button.text(us)+": "+state,null,in->button.click(in,us),on>0,button.enabled(us));
            button.renderExtra(us,d,x,y,buttonWidth,MyDraw.BUTTON_H);
            d.tooltip(x,y,buttonWidth,MyDraw.BUTTON_H,button.tooltip(us));
            x+=buttonWidth+MyDraw.BUTTON_SPACING;
        }
    }
    public static int resourceRowHeight(UniScreen us) {
        var ships=controlled(us);
        Airship ship=ships.size()==1?ships.getFirst():null;
        return ship!=null&&((ElectricalShip)ship).electricAge$summary()!=null?AGame.FOUNT.lineHeight:0;
    }
    public static void resourceRow(MyDraw d,Airship ship,int x,int y,int width) {
            PowerSummary s=((ElectricalShip)ship).electricAge$summary();if(s==null)return;
            String label=AcbricLanguage.text("Power:","电力：");
            String charge=s.capacity()>0?String.format(Locale.ROOT," %s %.0f%%",s.batteryRate()>1e-9?"+":s.batteryRate()< -1e-9?"-":"",100*s.stored()/s.capacity()):"";
            d.text(label,AGame.FOUNT,x,y);int labelW=(int)d.textSize(label,AGame.FOUNT).x+MyDraw.BUTTON_SPACING;
            int chargeW=(int)d.textSize(charge,AGame.FOUNT).x;
            d.text(charge,AGame.FOUNT,x+width-chargeW,y);
            x+=labelW;int w=Math.max(1,width-labelW-chargeW-MyDraw.BUTTON_SPACING),top=y+2;
            double denominator=Math.max(1,Math.max(s.available(),s.used()+s.unmet()+s.offline()));
            d.rect(MyDraw.DARK_BG,x-1,top-1,w+2,11);
            double used=w*s.used()/denominator,unmet=w*s.unmet()/denominator,offline=w*s.offline()/denominator;
            d.rect(GREEN,x,top,w*s.available()/denominator,6);d.rect(YELLOW,x,top,used,6);d.rect(RED,x+used,top,unmet,6);d.rect(GRAY,x+used+unmet,top,offline,6);
            for(double xx=x+used+unmet;xx<x+used+unmet+offline;xx+=5)d.rect(MyDraw.DARK_BG,xx,top,1,6);
            d.rect(GRAY,x,top+7,w,2);if(s.capacity()>0)d.rect(new Clr(126,198,255),x,top+7,w*s.stored()/s.capacity(),2);
            d.tooltip(x,top,w,10,AcbricLanguage.text("Electricity","电力")+String.format(Locale.ROOT," %.0f / %.0f kW | %.0f / %.0f kJ",s.used(),s.generation(),s.stored(),s.capacity()));
    }
    private static final class Bolts implements UniScreen.VisualLayer {
        public void tick(Input in,int ms,UniScreen us){}
        public void draw(MyDraw d,UniScreen us,double x,double y,double w,double h) {
            if(us.combat==null)return;
            int count=0;
            var graphics=(org.newdawn.slick.Graphics)d.frame().nativeRenderer();
            var oldColor=graphics.getColor();float oldWidth=graphics.getLineWidth();
            try {
            for(var b:LightningAttack.visuals(us.combat)) {
                double x1=b.x1(),y1=b.y1(),x2=b.x2(),y2=b.y2();
                // 图形预算只裁减屏幕上的表现，绝不读取战斗随机数。
                if(Math.max(x1,x2)<x||Math.max(y1,y2)<y||Math.min(x1,x2)>x+w||Math.min(y1,y2)>y+h)continue;
                if(count++>=64)break;
                int age=us.combat.time-b.born();int alpha=(int)(220*Math.max(0,1-age/180.0));
                Random visual=new Random(Double.doubleToLongBits(b.x2())^b.born()^(age/40));
                double px=x1,py=y1;
                for(int i=1;i<=12;i++) {
                    double nx=x1+(x2-x1)*i/12+(i==12?0:(visual.nextDouble()-.5)*8),ny=y1+(y2-y1)*i/12+(i==12?0:(visual.nextDouble()-.5)*8);
                    float thickness=(float)(1+StrictMath.sqrt(b.energy()/100.0));
                    graphics.setLineWidth(thickness+3);graphics.setColor(new org.newdawn.slick.Color(76,159,255,alpha/3));graphics.drawLine((float)px,(float)py,(float)nx,(float)ny);
                    graphics.setLineWidth(thickness);graphics.setColor(new org.newdawn.slick.Color(223,247,255,alpha));graphics.drawLine((float)px,(float)py,(float)nx,(float)ny);
                    px=nx;py=ny;
                }
            }
            } finally { graphics.setLineWidth(oldWidth);graphics.setColor(oldColor); }
        }
    }
}
