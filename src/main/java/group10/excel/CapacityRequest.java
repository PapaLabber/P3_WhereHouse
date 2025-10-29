// package group10.excel;

// import java.time.LocalDate;

// public class CapacityRequest {
//   private int palletAmount;
//   private LocalDate date;
//   private Temperature temperature;
//   private ProductionSite productionSite;

//   public CapacityRequest(int palletAmount, LocalDate date, Temperature temperature, ProductionSite productionSite) {
//     this.palletAmount = palletAmount;
//     this.date = date;
//     this.temperature = temperature;
//     this.productionSite = productionSite;
//   }
// }

package group10.excel;


/**
 * Represents ONE valid filtered row from the Excel file.
 */
public class CapacityRequest {

    private final int palletAmount;
    private final Temperature temperature;
    private final ProductionSite productionSite;

    public CapacityRequest(int palletAmount,
                           Temperature temperature,
                           ProductionSite productionSite) {
        this.palletAmount = palletAmount;
        this.temperature = temperature;
        this.productionSite = productionSite;
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

    @Override
    public String toString() {
        return String.format(
            "CapacityRequest{palletAmount=%d, temperatureZone=%s, productionSite=%s}",
            palletAmount,
            temperature,
            productionSite
        );
    }
}

