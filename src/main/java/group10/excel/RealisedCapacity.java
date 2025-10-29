package group10.excel;

import java.time.LocalDate;

public class RealisedCapacity {
  private int palletAmount;
  private LocalDate date;
  private Temperature temperature;
  private Warehouse warehouse;

  public RealisedCapacity(int palletAmount, LocalDate date, Temperature temperature, Warehouse warehouse) {
    this.palletAmount = palletAmount;
    this.date = date; // Behøves dette eller skal vi bruge datoen?
    this.temperature = temperature;
    this.warehouse = warehouse;
  }

  public int getPalletAmount() {
    return this.palletAmount;
  }

  public LocalDate getDate() {
    return this.date;
  }

  public Temperature getTemperature() {
    return this.temperature;
  }

  public Warehouse getWarehouse() {
    return this.warehouse;
  }

}
