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

  // Getters
  public Warehouse getWarehouse() {
    return warehouse;
  }

  public ProductionSite getProductionSite() {
    return productionSite;
  }

  public double getKilometers() {
    return kilometers;
  }

  // Setters
  public void setWarehouse(Warehouse warehouse) {
    this.warehouse = warehouse;
  }

  public void setProductionSite(ProductionSite productionSite) {
    this.productionSite = productionSite;
  }

  public void setKilometers(double kilometers) {
    this.kilometers = kilometers;
  }
}