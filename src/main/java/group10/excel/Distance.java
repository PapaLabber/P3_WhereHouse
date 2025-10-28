package group10.excel;

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
