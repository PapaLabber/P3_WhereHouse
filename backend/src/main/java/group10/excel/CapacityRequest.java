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
        return this.palletAmount;
    }

    public Temperature getTemperature() {
        return this.temperature;
    }

    public ProductionSite getProductionSite() {
        return this.productionSite;
    }

    public int getID() {
        return this.ID;
    }

    public int getYear() {
        return this.year;
    }

    @Override
    public String toString() {
        return String.format(
                "ID = %d, pallet Amount = %d, Storage Condition = %s, Production Site = %s, Year = %d",
                this.ID,
                this.palletAmount,
                this.temperature,
                this.productionSite,
                this.year
        );
    }
}
