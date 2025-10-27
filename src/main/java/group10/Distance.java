package group10;

import group10.ProductionSite;

public class Distance {
  private Warehouse warehouse;
  private ProductionSite productionSite;
  private double kilometers;

  public Distance(Warehouse warehouse, ProductionSite productionSite, double kilometers) {
    this.warehouse = warehouse;
    this.productionSite = productionSite;
    this.kilometers = kilometers;
  }
}
