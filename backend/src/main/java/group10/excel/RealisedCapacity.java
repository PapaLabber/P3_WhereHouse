package group10.excel;

public class RealisedCapacity {
  private int palletAmount;
  private Temperature temperature;
  private Warehouse warehouse;
  private int year;

  public RealisedCapacity(int palletAmount, Temperature temperature, Warehouse warehouse, int year) {
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
