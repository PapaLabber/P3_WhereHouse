package group10.excel;

public class ProductionSite {
  private String name;
  private double longitude;
  private double latitude;

  public ProductionSite(String name, double longitude, double latitude) {
    this.name = name;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public final void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public final void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getLongitude() {
    return this.longitude;
  }

  public double getLatitude() {
    return this.latitude;
  }

  public String getName() {
    return this.name;
  }

}
