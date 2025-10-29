// package group10.excel;

// public class Temperature {
//   private String ambient;
//   private String cold;
//   private String freeze;

 
//   public Temperature(String ambient, String cold, String freeze) {
//     this.ambient = ambient;
//     this.cold = cold;
//     this.freeze = freeze;
//   }

//   public String getAmbient() {
//     return ambient;
//   }

//   public String getCold() {
//     return cold;
//   }

//   public String getFreeze() {
//     return freeze;
//   }

//   @Override
//     public String toString() {
//         return String.format("Temperature{ambient='%s', cold='%s', freeze='%s'}", ambient, cold, freeze);
//     }

// }

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
