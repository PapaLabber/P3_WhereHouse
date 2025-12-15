package group10.excel;

/**
 * Represents the realized capacity of one warehouse
 */
public class RealizedCapacity {
  private int palletAmount;
  private Temperature temperature;
  private Warehouse warehouse;
  private int year;

  public RealizedCapacity(int palletAmount, Temperature temperature, Warehouse warehouse, int year) {
    this.palletAmount = palletAmount;
    this.temperature = temperature;
    this.warehouse = warehouse;
    this.year = year;
  }

  public int getPalletAmount() {
    return this.palletAmount;
  }

  public Temperature getTemperature() {
    return this.temperature;
  }

  public Warehouse getWarehouse() {
    return this.warehouse;
  }

  public int getYear() {
    return this.year;
  }

  @Override
  public String toString() {
    return String.format(
        "Pallet Amount = %d, Storage Condition = %s, Warehouse = %s",
        this.palletAmount,
        this.temperature,
        this.warehouse
    );
  }
}
