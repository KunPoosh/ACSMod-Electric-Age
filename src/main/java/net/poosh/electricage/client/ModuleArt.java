/* ModuleArt.java — 状态贴图仅消费实例快照；图集来自已交付分层 PNG。 */
package net.poosh.electricage.client;
import com.zarkonnen.airships.*;
import com.zarkonnen.airships.Module;
import net.poosh.electricage.integration.*;
import net.poosh.electricage.simulation.*;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ModuleArt {
    private static final JSONObject ATLAS;
    private static final Map<String,Appearance> CACHE=new HashMap<>();
    private static SpritesheetBundle sheet;
    static {
        try(var in=ModuleArt.class.getResourceAsStream("/electric_age/atlas.json")) {
            if(in==null)throw new IllegalStateException("Missing electrical atlas metadata");
            ATLAS=new JSONObject(new String(in.readAllBytes(),StandardCharsets.UTF_8));
        }catch(java.io.IOException ex){throw new ExceptionInInitializerError(ex);}
    }
    private ModuleArt(){}
    public static double token(Module m) {
        DeviceState s=ElectricRuntime.state(m);if(s==null)return 1;
        int state=0;
        if(s.spec.kind()==DeviceSpec.Kind.BATTERY) {
            int level=(int)Math.floor(5*s.energy/s.spec.capacity()+1e-9);
            int flow=s.power>1e-9?1:s.power< -1e-9?2:0;
            return -(1+level+flow*6+(m.hp<m.getMaxHP()*.75?18:0));
        }
        else if(((ElectricalModule)m).electricAge$factor()>0)state=s.power>s.spec.rated()&&s.spec.kind()==DeviceSpec.Kind.LOAD?3:
            s.spec.kind()==DeviceSpec.Kind.LOAD&&s.power<s.spec.rated()?2:1;
        return -(1+state+(m.hp<m.getMaxHP()*.75?4:0));
    }
    public static Appearance appearance(ModuleType type,double token) {
        String key=type.name.replace("FLIPPED_","");if(!key.startsWith("EA_"))return null;
        key=key.substring(3).toLowerCase(Locale.ROOT);if(!ATLAS.has(key))return null;
        int state=token<0?(int)(-token-1):key.equals("battery")?5:1;
        if(state<0||state>=ATLAS.getJSONObject(key).getInt("states"))throw new IllegalArgumentException("Invalid electrical render token");
        SpritesheetBundle current=SpritesheetBundle.ofName("ea_devices");
        if(current!=sheet){CACHE.clear();sheet=current;}
        boolean flipped=type.name.startsWith("FLIPPED_");
        String cacheKey=key+":"+state+":"+flipped;
        Appearance app=CACHE.get(cacheKey);
        if(app==null) {
            JSONObject info=ATLAS.getJSONObject(key);int w=info.getInt("w")/16,h=info.getInt("h")/16,y=info.getInt("y")/16+state*h,x=info.getInt("x")/16;
            org.json.JSONArray frames=new org.json.JSONArray();
            for(int i=0;i<4;i++)frames.put(new JSONObject().put("x",x+i*w).put("y",y).put("w",w).put("h",h));
            app=new Appearance(new JSONObject().put("src","ea_devices").put("interval",100).put("frames",frames));
            if(flipped)app=app.flip();CACHE.put(cacheKey,app);
        }
        return app;
    }
}
