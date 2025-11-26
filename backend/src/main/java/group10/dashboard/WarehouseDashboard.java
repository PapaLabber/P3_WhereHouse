package group10.dashboard;

/**
 * DTO representing the dashboard view for a single warehouse.
 * This is what the frontend receives in /api/dashboard.
 */
public class WarehouseDashboard {

    private final String warehouseName;
    private final int totalCapacity;
    private final int usedCapacity;
    private final int remainingCapacity;
    private final double utilisationPercent;
    private final TemperatureDashboard temperatureDashboard;

    public WarehouseDashboard(String warehouseName,
                              int totalCapacity,
                              int usedCapacity,
                              int remainingCapacity,
                              double utilisationPercent,
                              TemperatureDashboard temperatureDashboard) {
        this.warehouseName = warehouseName;
        this.totalCapacity = totalCapacity;
        this.usedCapacity = usedCapacity;
        this.remainingCapacity = remainingCapacity;
        this.utilisationPercent = utilisationPercent;
        this.temperatureDashboard = temperatureDashboard;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getUsedCapacity() {
        return usedCapacity;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public double getUtilisationPercent() {
        return utilisationPercent;
    }

    public TemperatureDashboard getTemperatureDashboard() {
        return temperatureDashboard;
    }
}
