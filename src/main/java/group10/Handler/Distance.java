package group10.Handler;

import group10.Clases.ProductionSite;
import group10.Clases.Warehouse;

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
