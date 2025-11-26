package group10.dashboard;

import group10.excel.Temperature;

import java.util.EnumMap;
import java.util.Map;

/**
 * Simple DTO representing how many pallets are stored per temperature category
 * for a single warehouse on the dashboard.
 */
public class TemperatureDashboard {

    private final int ambient;
    private final int cold;
    private final int freeze;

    public TemperatureDashboard(int ambient, int cold, int freeze) {
        this.ambient = ambient;
        this.cold = cold;
        this.freeze = freeze;
    }

    public int getAmbient() {
        return ambient;
    }

    public int getCold() {
        return cold;
    }

    public int getFreeze() {
        return freeze;
    }

    /**
     * Helper to build a TemperatureDashboard object from a map of Temperature -> amount.
     */
    public static TemperatureDashboard fromMap(Map<Temperature, Integer> map) {
        // ensure we don't get NullPointerExceptions
        Map<Temperature, Integer> safe = new EnumMap<>(Temperature.class);
        if (map != null) {
            safe.putAll(map);
        }

        int ambient = safe.getOrDefault(Temperature.AMBIENT, 0);
        int cold = safe.getOrDefault(Temperature.COLD, 0);
        int freeze = safe.getOrDefault(Temperature.FREEZE, 0);

        return new TemperatureDashboard(ambient, cold, freeze);
    }
}
