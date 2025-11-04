
package group10.excel;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a fixed production site with a name and coordinates.
 * All valid sites are predefined here.
 *
 * You do NOT new this class directly. You call ProductionSite.fromName("Hillerød")
 * and get the canonical object, or null if it's not a known site.
 */
public class ProductionSite {

    private final String name;
    private final double longitude;
    private final double latitude;

    // Private constructor: only this class can create sites.
    private ProductionSite(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // --- Public getters ---

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    @Override
    public String toString() {
        return String.format(
            "ProductionSite{name='%s', longitude=%.4f, latitude=%.4f}",
            name, longitude, latitude
        );
    }

    // --- Static registry of allowed production sites ---

    private static final Map<String, ProductionSite> SITES = new HashMap<>();

    static {
        // IMPORTANT:
        // Keys are lowercase so we can do case-insensitive lookup.
        // The string we pass into the ProductionSite constructor is the
        // canonical display name you want in the rest of the app.

        SITES.put("hillerød",   new ProductionSite("Hillerød",   12.0262, 55.8838));
        SITES.put("kalundborg", new ProductionSite("Kalundborg", 11.3345, 55.6720));
        SITES.put("hjørring",   new ProductionSite("Hjørring",   12.5860, 57.4590));
        SITES.put("bagsværd",   new ProductionSite("Bagsværd",   12.0333, 55.8833));
        SITES.put("gentofte",   new ProductionSite("Gentofte",   12.5333, 55.7500));

        // Add more sites here if they exist in your Excel file.
        // The IMPORTANT thing is that the strings here match exactly
        // what appears in the "ProductionSite" column in Excel
        // (ignoring case and leading/trailing spaces).
    }

    /**
     * Lookup a ProductionSite object by the site name string from Excel.
     * Trims whitespace and matches case-insensitively.
     *
     * Example:
     *   ProductionSite.fromName("Hillerød")
     *   ProductionSite.fromName("hillerød")
     *   ProductionSite.fromName("  Hillerød  ")
     * all return the same ProductionSite instance.
     *
     * Returns null if the site is unknown.
     */
    public static ProductionSite fromName(String siteName) {
        if (siteName == null) return null;
        String key = siteName.trim().toLowerCase();
        return SITES.get(key);
    }

    /**
     * Returns true if this site name is a known, valid production site.
     */
    public static boolean isKnown(String siteName) {
        if (siteName == null) return false;
        String key = siteName.trim().toLowerCase();
        return SITES.containsKey(key);
    }
}
