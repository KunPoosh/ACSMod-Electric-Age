/* Balance.java — 从随包配置加载唯一电力数值基线，不依赖游戏或本机文件。 */
package net.poosh.electricage.simulation;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import static net.poosh.electricage.simulation.DeviceSpec.Kind.*;
import static net.poosh.electricage.simulation.DeviceSpec.Group.*;

public final class Balance {
    public static final Balance DEFAULT = new Balance();
    private final Properties values = new Properties();
    private final Map<String, DeviceSpec> specs = new LinkedHashMap<>();
    public final double linear, quadratic, wear;
    private Balance() {
        try (InputStream in = Balance.class.getResourceAsStream("/electric_age/balance.properties")) {
            if (in == null) throw new IllegalStateException("Missing Electric Age balance");
            values.load(in);
        } catch (IOException e) { throw new ExceptionInInitializerError(e); }
        linear = value("overload.linear"); quadratic = value("overload.quadratic"); wear = value("overload.wear");
        if (Math.abs(linear + quadratic - 1) > 1e-12) throw new IllegalArgumentException("Overload curve must reach one");
        specs.put("generator", new DeviceSpec("generator", GENERATOR, NONE, 0,0,0,0,value("generator.output"),0,0,0));
        specs.put("battery", new DeviceSpec("battery", BATTERY, NONE,0,0,0,0,0,value("battery.capacity"),value("battery.discharge"),value("battery.charge")));
        for (String key : new String[]{"cannon","lightning","propulsion","lift"})
            specs.put(key, new DeviceSpec(key,LOAD, key.equals("cannon") || key.equals("lightning") ? WEAPONS : PROPULSION,
                    value(key+".rated"),value(key+".minimum"),value(key+".overload"),value(key+".bonus"),0,0,0,0));
    }
    public double value(String key) {
        String text = values.getProperty(key);
        if (text == null) throw new IllegalArgumentException("Missing balance key: " + key);
        double v = Double.parseDouble(text);
        if (!Double.isFinite(v) || v < 0) throw new IllegalArgumentException("Invalid balance key: " + key);
        return v;
    }
    public DeviceSpec spec(String key) {
        DeviceSpec result = specs.get(key);
        if (result == null) throw new IllegalArgumentException("Unknown electrical device: " + key);
        return result;
    }
}
