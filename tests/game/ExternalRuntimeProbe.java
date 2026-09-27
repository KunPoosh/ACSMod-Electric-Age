/* ExternalRuntimeProbe.java — 隔离实际游戏、生产定义、模拟与 GPU；不修改游戏安装。 */
package net.fabricacs.regression;
import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.integration.*;
import net.poosh.electricage.simulation.*;
import org.json.*;
import java.nio.file.*;
import java.util.*;
import org.lwjgl.opengl.*;
public final class ExternalRuntimeProbe {
 static int checks,frames,glErrors,stage;static boolean started,done;static Runnable pending;
 static com.zarkonnen.catengine.Input input;static KHRDebugCallback trace;static Combat combat;
 static Path instance(){return Path.of(System.getProperty("acbric.external.instance"));}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;System.out.println("PASS electrical game: "+s);}
 static void near(double a,double b,String s){check(Math.abs(a-b)<1e-7,s+" ("+a+")");}
 static AirshipGame game()throws Exception{var f=AirshipGame.class.getDeclaredField("instance");f.setAccessible(true);return (AirshipGame)f.get(null);}
 public static void main(String[] args)throws Exception{Main.main(args);throw new AssertionError("No checkpoint");}
 public static void inputComplete(com.zarkonnen.catengine.Input in){input=in;Runnable r=pending;pending=null;if(r!=null)r.run();}
 public static void menuRendered(){if(!started&&++frames>=30){started=true;pending=ExternalRuntimeProbe::ready;}}
 static void fail(Throwable e){e.printStackTrace();System.exit(2);}
 static Module add(Airship s,String name,int x,int y){
  Module m=new Module(s,ModuleType.ofName(name),x,y);s.modules.add(m);
  for(int yy=0;yy<m.type.getH();yy++)for(int xx=0;xx<m.type.getW();xx++){
   Tile t=new Tile(s,m,x+xx,y+yy);t.armour.setType(m.type.getArmourType()!=null?m.type.getArmourType():ArmourType.ofName("LT_WOOD"));s.tiles.add(t);
  }
  return m;
 }
 static Airship vessel() {
  Airship s=new Airship(ShipType.AIRSHIP);s.setName("Electric integration vessel");
  add(s,"EA_LIGHTNING",4,0);add(s,"EA_PROPULSION",0,3);add(s,"EA_GENERATOR",2,3);add(s,"EA_LIFT",5,3);add(s,"EA_CANNON",8,3);
  add(s,"EA_BATTERY",0,5);add(s,"COAL_STORE",2,5);add(s,"AMMO",5,5);add(s,"BRIDGE",8,5);add(s,"QUARTERS",8,6);
  s.layout();s.repair(true);s.msSinceOnGround=10000;s.enginesRunning=true;s.suspendiumRunning=true;return s;
 }
 static void ready(){try{
  check(Display.isCreated()&&org.lwjgl.openal.AL.isCreated(),"native GPU and audio");
  if(GLContext.getCapabilities().GL_KHR_debug){trace=new KHRDebugCallback((a,b,c,d,message)->{if(b==KHRDebug.GL_DEBUG_TYPE_ERROR&&++glErrors==1){System.err.println("ELECTRIC GL ERROR: "+message);new Exception("GL caller").printStackTrace();}});KHRDebug.glDebugMessageCallback(trace);GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT);GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);}
  System.out.println("MODS: "+Mod.getEnabledModIDs());
  for(String key:List.of("GENERATOR","BATTERY","PROPULSION","LIFT","CANNON","LIGHTNING")) {
   ModuleType t=ModuleType.ofName("EA_"+key);check(t!=null,"production definition "+key);
   check(t.getApp(Tech.getStandardBonuses(),1)!=null,"production appearance "+key);
  }
  check(Combat.EXECS.containsKey(OverloadCommands.TYPE),"native executor registration");
  for(String name:List.of("ea_devices","ea_propeller","ea_cannon_barrel")) {
   var ssb=SpritesheetBundle.ofName(name);
   check(ssb.bumpTex!=null,"native bump texture loaded "+name);
   check(ssb.getDamagedVersion()!=null&&ssb.getDamagedVersion().getTex("")!=null,"native damaged sheet loaded "+name);
   check(ssb.getFragmentsSheet()!=null&&ssb.getFragmentsSheet().getTex("")!=null,"native fragments sheet loaded "+name);
  }
  String warnings=ModuleType.ofName("EA_LIFT").sourceMod.getWarnings();
  Files.writeString(instance().resolve("mod-warnings.txt"),warnings);
  check(warnings.isBlank(),"companion MOD reports no missing-sheet warnings");
  for(String key:List.of("GENERATOR","BATTERY","PROPULSION","LIFT","CANNON","LIGHTNING"))
   check(ModuleType.ofName("EA_"+key).getAppFragments(BonusSet.empty()).stream().anyMatch(a->!a.isEmpty()),"native fragment mapping populated "+key);
  check(Tech.findProvider(ModuleType.ofName("EA_LIGHTNING").getRequired()).name.equals("MACHINING"),"native machining unlock requirement");
  check(ModuleType.ofName("EA_LIGHTNING").getBlastDmg(BonusSet.empty())==(int)Balance.DEFAULT.value("lightning.energy"),"weapon panel damage derives from attack energy");
  check(Lang._t("mod_desc_EA_LIGHTNING").contains("24 / 40 / 60"),"module description exposes configured power thresholds");
  var flipped=Appearance.class.getDeclaredField("isFlipped");flipped.setAccessible(true);
  check(flipped.getBoolean(ModuleType.ofName("FLIPPED_EA_CANNON").getApp(BonusSet.empty(),1)),"mirrored cannon uses mirrored state art");
  check(ModuleType.ofName("EA_LIGHTNING").getArmourType().hp.get(BonusSet.empty())==0,"deck lightning armour grants no HP");
  combat=new Combat(game(),TimeOfDay.ofName("DAY"));combat.setRandomSeed(89412L);
  var terrain=LandFormation.generate(new GuardedRandom(89412L),false,LandscapeType.ofName("GRASSLAND"));combat.landFormations.add(terrain.a);combat.landFormations.addAll(terrain.b);
  for(int i=0;i<2;i++){Airship s=vessel();s.networkID="electric-"+i;s.setX(i==0?-220:80);s.setY(-100);s.flipped=i==1;s.flipTo=s.flipped;combat.sides.get(i).ships.add(s);}
  Files.writeString(instance().resolve("Electric Age Test Ship.json"),combat.sides.get(0).ships.get(0).toJSON(combat,false).toString(2));
  combat.initWheelsLegsTentaclesAndBarrels();
  for(int i=0;i<100;i++)combat.tick(16,combat.sides.get(0),0,1);
  Airship s=combat.sides.get(0).ships.get(0);
  check(((ElectricalShip)s).electricAge$result()!=null,"real combat tick solved grid");
  near(((ElectricalShip)s).electricAge$result().generation(),220,"staffed production generator supplies 220 kW");
  check(ShipEditorUtils.getStats(s,99999,null).stream().anyMatch(p->p.a.equals("EA_POWER")),"native builder statistics row");
  Module b=s.modules.stream().filter(m->"battery".equals(ElectricRuntime.key(m))).findFirst().orElseThrow();
  ElectricRuntime.state(b).energy=321;
  s.commandPoints=s.commandPointsRequired();check(s.readyForCommand(),"native bridge ready for command");
  OverloadCommands.send(combat,List.of(s),true,true);
  check(OverloadCommands.requested(s,true)&&s.commandPoints==0,"native dispatch enables overload and spends command");
  s.commandPoints=s.commandPointsRequired();int cp=s.commandPoints;
  OverloadCommands.send(combat,List.of(s),true,true);check(s.commandPoints==cp,"repeated target state spends no command");
  OverloadCommands.send(combat,List.of(s),false,true);check(OverloadCommands.requested(s,false)&&s.commandPoints==0,"both overload groups coexist");
  JSONObject saved=s.toJSON(combat,false);Airship restored=new Airship(saved);
  Module rb=restored.modules.stream().filter(m->"battery".equals(ElectricRuntime.key(m))).findFirst().orElseThrow();
  near(ElectricRuntime.state(rb).energy,ElectricRuntime.state(b).energy,"full native ship JSON preserves charge");
  check(OverloadCommands.requested(restored,true)&&OverloadCommands.requested(restored,false),"full ship JSON preserves overload requests");
  restored.repairPartially();near(ElectricRuntime.state(rb).energy,321,"native partial post-battle repair preserves charge");
  rb.fillUpResources();near(ElectricRuntime.state(rb).energy,2000,"native resupply restores full charge");
  check(restored.switchSides()&&!OverloadCommands.requested(restored,true)&&!OverloadCommands.requested(restored,false),"native capture clears overload requests");
  splitCheck();
  Airship enemy=combat.sides.get(1).ships.get(0);s.fireAt=enemy;
  for(Tile t:enemy.tiles)t.armour.setType(ArmourType.ofName("EA_UNARMORED"));
  int hp=enemy.modules.stream().mapToInt(m->m.hp).sum();
  Module lightning=s.modules.stream().filter(m->"lightning".equals(ElectricRuntime.key(m))).findFirst().orElseThrow();
  check(LightningAttack.fire(lightning,combat,1),"native lightning aim produces attack");
  check(enemy.modules.stream().mapToInt(m->m.hp).sum()<hp,"lightning immediately applies native point damage");
  check(LightningAttack.visuals(combat).size()>1&&LightningAttack.visuals(combat).size()<=5,"branching attack respects five-node budget");
  check(combat.shots.stream().noneMatch(shot->shot.weapon==lightning),"instant lightning leaves no travel projectile");
  deterministicCheck();
  ElectricRuntime.tick(s,16);
  UniScreen screen=new UniScreen(game(),new SingleCombatIntent());screen.combat=combat;screen.mySide=combat.sides.get(0);screen.selectedShip=s;
  buttonCheck(screen,s);
  combat.speed=CombatSpeed.STOP;game().s=screen;ZoomToFitButton.zoomToFit(input,screen,1.0);frames=0;
 }catch(Throwable e){fail(e);}}
 static void buttonCheck(UniScreen screen,Airship s)throws Exception {
  var buttons=List.of(new net.poosh.electricage.client.ElectricUi.OverloadButton(true),new net.poosh.electricage.client.ElectricUi.OverloadButton(false));
  check(screen.buttons.stream().noneMatch(b->b instanceof net.poosh.electricage.client.ElectricUi.OverloadButton) && buttons.stream().allMatch(b->b.visible(screen)) && net.poosh.electricage.client.ElectricUi.overloadRowHeight(screen)>0,"overload controls reserve a ship-panel row instead of top-bar buttons");
  Airship second=new Airship(s.toJSON(combat,false));second.networkID="multi-select";((ElectricalShip)second).electricAge$request(new PowerGrid.Request(false,false));
  combat.sides.get(0).ships.add(second);second.commandPoints=second.commandPointsRequired();int secondPoints=second.commandPoints;
  s.commandPoints=s.commandPointsRequired();screen.selectedShip=null;screen.selectedShips.add(s);screen.selectedShips.add(second);
  var button=buttons.getFirst();check(button.selected(screen)&&button.enabled(screen),"mixed selection advertises turn-off action");
  button.click(input,screen);
  check(!OverloadCommands.requested(s,true)&&!OverloadCommands.requested(second,true)&&s.commandPoints==0&&second.commandPoints==secondPoints,"mixed toggle changes only enabled ships and charges once");
  screen.selectedShips.clear();screen.selectedShip=s;combat.sides.get(0).ships.remove(second);
  ((ElectricalShip)s).electricAge$request(new PowerGrid.Request(true,true));
 }
 static void deterministicCheck()throws Exception {
  String json=combat.toJSON().toString();Combat left=new Combat(game(),new JSONObject(json)),right=new Combat(game(),new JSONObject(json));
  left.initWheelsLegsTentaclesAndBarrels();right.initWheelsLegsTentaclesAndBarrels();
  for(Combat c:List.of(left,right)) {
   Airship ship=c.sides.get(0).ships.get(0);ship.commandPoints=ship.commandPointsRequired();OverloadCommands.send(c,List.of(ship),true,false);
  }
  for(int i=0;i<120;i++) {
   left.tick(16,left.sides.get(0),0,1);right.tick(16,right.sides.get(0),0,1);
   check(left.cheapHash()==right.cheapHash(),"two restored native simulations match at tick "+i);
  }
  for(int side=0;side<2;side++)for(int mi=0;mi<left.sides.get(side).ships.get(0).modules.size();mi++) {
   Module a=left.sides.get(side).ships.get(0).modules.get(mi),b=right.sides.get(side).ships.get(0).modules.get(mi);
   if(ElectricRuntime.key(a)!=null)check(ElectricRuntime.save(a).toString().equals(ElectricRuntime.save(b).toString()),"restored electrical state matches after native simulation "+side+":"+mi);
  }
 }
 static void splitCheck()throws Exception {
  Airship s=new Airship(ShipType.AIRSHIP);s.setName("Split probe");s.networkID="split-probe";
  add(s,"EA_GENERATOR",0,0);Module b=add(s,"EA_BATTERY",3,0);add(s,"EA_PROPULSION",20,0);add(s,"EA_LIGHTNING",22,0);
  s.layout();s.repair(true);ElectricRuntime.state(b).energy=321;
  ((ElectricalShip)s).electricAge$request(new PowerGrid.Request(true,true));
  int count=combat.sides.get(0).ships.size();combat.sides.get(0).ships.add(s);
  var split=Airship.class.getDeclaredMethod("splitIfNeeded",Combat.class,boolean.class);split.setAccessible(true);split.invoke(s,combat,true);
  check(combat.sides.get(0).ships.size()==count+2,"native split creates fragment");
  double energy=0;
  for(Airship fragment:new ArrayList<>(combat.sides.get(0).ships.subList(count,combat.sides.get(0).ships.size()))) {
   check(OverloadCommands.requested(fragment,true)&&OverloadCommands.requested(fragment,false),"fragment inherits both requests before first tick");
   check(fragment.toJSON(combat,false).getJSONObject("electric_age").getBoolean("weapons"),"fragment can immediately save inherited request");
   for(Module m:fragment.modules)if(ElectricRuntime.state(m)!=null)energy+=ElectricRuntime.state(m).energy;
   combat.sides.get(0).ships.remove(fragment);
  }
  near(energy,321,"native splitting conserves total battery charge");
 }
 public static void battleRendered(UniScreen screen){if(done)return;if(++frames>=30)pending=()->finish(screen);}
 static Airship armourComparison() {
  Airship s=new Airship(ShipType.AIRSHIP);s.setName("Native above / Electric below");
  add(s,"SUSPENDIUM_CHAMBER",0,0);add(s,"SUSPENDIUM_CHAMBER",3,0);
  add(s,"EA_LIFT",0,2);add(s,"EA_LIFT",3,2);
  for(Tile t:s.tiles)t.armour.setType(ArmourType.ofName(t.x<3?"LT_WOOD":"LT_STEEL"));
  s.layout();s.repair(true);return s;
 }
 static void finish(UniScreen screen){try{
  check(frames>=30,"rendered 30 frames: "+List.of("battle","editor","armour comparison","destroyed modules").get(stage));
  int w=Display.getWidth(),h=Display.getHeight();var pixels=org.lwjgl.BufferUtils.createByteBuffer(w*h*4);GL11.glReadPixels(0,0,w,h,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
  var image=new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_ARGB);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){int i=(y*w+x)*4;image.setRGB(x,h-y-1,0xff000000|((pixels.get(i)&255)<<16)|((pixels.get(i+1)&255)<<8)|(pixels.get(i+2)&255));}
  javax.imageio.ImageIO.write(image,"png",instance().resolve(List.of("battle.png","editor.png","armour.png","destroyed.png").get(stage)).toFile());
  if(stage==0){
   UniScreen editor=new UniScreen(game(),new StandaloneEditShipIntent());editor.standaloneEditShip=vessel();game().s=editor;
   ZoomToFitButton.zoomToEditShip(input,editor);stage=1;frames=0;return;
  }
  if(stage==1){
   screen.standaloneEditShip=armourComparison();screen.tool=new PlaceArmourTool(ArmourType.ofName("LT_STEEL"));
   ((EditShipIntent)screen.intent).mode=EditMode.ARMOUR;
   ZoomToFitButton.zoomToEditShip(input,screen);stage=2;frames=0;return;
  }
  if(stage==2){
   Airship s=combat.sides.get(0).ships.get(0);for(Module m:s.modules)if(ElectricRuntime.key(m)!=null)m.hp=0;
   UniScreen battle=new UniScreen(game(),new SingleCombatIntent());battle.combat=combat;battle.mySide=combat.sides.get(0);battle.selectedShip=s;
   game().s=battle;battle.zoom=3;battle.scrollX=-s.getIntX()-s.getWidth()*8;battle.scrollY=-s.getIntY()-s.getHeight()*8;
   stage=3;frames=0;return;
  }
  done=true;
  Files.writeString(instance().resolve("electric-checkpoint.json"),new JSONObject().put("checks",checks).put("battleFrames",30).put("editorFrames",30).put("armourFrames",30).put("destroyedFrames",frames).put("glErrors",glErrors).put("renderer",GL11.glGetString(GL11.GL_RENDERER)).toString(2));
  System.out.println("ELECTRIC GAME PASS: "+checks);System.exit(0);
 }catch(Throwable e){fail(e);}}
}
