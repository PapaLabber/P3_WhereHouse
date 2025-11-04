package group10.excel;


public class RealisedCapacity {
  private int palletAmount;
  private Temperature temperature;
  private Warehouse warehouse;

  public RealisedCapacity(int palletAmount, Temperature temperature, Warehouse warehouse) {
    this.palletAmount = palletAmount;
    this.temperature = temperature;
    this.warehouse = warehouse;
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

}
