

package group10.excel;

/**
 * Storage temperature requirement for a capacity request.
 * Comes directly from the "Temperature" column in Excel.
 *
 * Valid values in Excel:
 *   Ambient
 *   Cold
 *   Freeze
 */
public enum Temperature {
    AMBIENT,
    COLD,
    FREEZE;

    /**
     * Convert a raw string from Excel into a TemperatureZone.
     * Returns null if it's not a recognized value.
     */
    public static Temperature fromString(String raw) {
        if (raw == null) {
            return null;
        }

        String norm = raw.trim().toLowerCase();

        switch (norm) {
            case "ambient":
                return AMBIENT;
            case "cold":
                return COLD;
            case "freeze":
            case "frozen": // handle sloppy data like "Frozen"
                return FREEZE;
            default:
                return null;
        }
    }
}
