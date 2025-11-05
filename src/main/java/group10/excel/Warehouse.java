package group10.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Warehouse {

    private String name;
    private double longitude;
    private double latitude;
    private List<RealisedCapacity> realisedCapacities = new ArrayList<>();

    public Warehouse(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public final void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public final void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void addRealisedCapacity(RealisedCapacity rc) {
        realisedCapacities.add(rc);
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public List<RealisedCapacity> getRealisedCapacities() {
        return realisedCapacities;
    }

    public String getName() {
        return this.name;
    }

    private static final Map<String, Warehouse> WAREHOUSES = new HashMap<>();

    static {
        // IMPORTANT:
        // Keys are lowercase so we can do case-insensitive lookup.
        // The string we pass into the ProductionSite constructor is the
        // canonical display name you want in the rest of the app.

        WAREHOUSES.put("ps hub",    new Warehouse("PS HUB", 12.34, 55.67));
        WAREHOUSES.put("ps pac i",  new Warehouse("PS PAC I", 12.34, 55.67));
        WAREHOUSES.put("ps pac ii", new Warehouse("PS PAC II", 12.34, 55.67));
        WAREHOUSES.put("neff",      new Warehouse("NEFF", 12.34, 55.67));

        // Add more sites here if they exist in your Excel file.
        // The IMPORTANT thing is that the strings here match exactly
        // what appears in the "ProductionSite" column in Excel
        // (ignoring case and leading/trailing spaces).
    }

    /**
     * Lookup a ProductionSite object by the site name string from Excel. Trims
     * whitespace and matches case-insensitively. Returns null if the site is
     * unknown.
     */
    public static Warehouse fromName(String warehouseName) {
        if (warehouseName == null) {
            return null;
        }
        String key = warehouseName.trim().toLowerCase();
        return WAREHOUSES.get(key);
    }

    /**
     * Returns true if this site name is a known, valid production site.
     */
    public static boolean isKnown(String warehouseName) {
        if (warehouseName == null) {
            return false;
        }
        String key = warehouseName.trim().toLowerCase();
        return WAREHOUSES.containsKey(key);
    }
@Override
public String toString() {
    return name; // or String.format("%s (%.5f, %.5f)", name, latitude, longitude);
}
}
