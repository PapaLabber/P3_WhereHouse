package group10;

import java.lang.invoke.ClassSpecializer.Factory;

public class Distance {
  private Warehouse warehouse;
  private Factory factory;
  private double kilometers;

  public Distance(Warehouse warehouse, Factory factory, double kilometers) {
    this.warehouse = warehouse;
    this.factory = factory;
    this.kilometers = kilometers;
  }

  public Warehouse getWarehouse() {
    return this.warehouse;
  }

  public Factory getFactory() {
    return this.factory;
  }

  public double getKilometers() {
    return this.kilometers;
  }


}
