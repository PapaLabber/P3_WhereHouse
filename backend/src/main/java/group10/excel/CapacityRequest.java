package group10.excel;

/**
 * Represents ONE valid filtered row from the Excel file.
 */
public class CapacityRequest {

    private final int palletAmount;
    private final Temperature temperature;
    private final ProductionSite productionSite;
    private final int ID;
    private final int year;

    public CapacityRequest(int palletAmount, Temperature temperature, ProductionSite productionSite, int ID, int year) {
        this.palletAmount = palletAmount;
        this.temperature = temperature;
        this.productionSite = productionSite;
        this.ID = ID;
        this.year = year;
    }

    public int getPalletAmount() {
        return palletAmount;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public ProductionSite getProductionSite() {
        return productionSite;
    }

    public int getID() {
        return ID;
    }

    public int getYear() {
        return year;
    }

    @Override
    public String toString() {
        return String.format(
                "ID = %d, pallet Amount = %d, Storage Condition = %s, Production Site = %s, Year = %d",
                ID,
                palletAmount,
                temperature,
                productionSite,
                year
        );
    }
}
