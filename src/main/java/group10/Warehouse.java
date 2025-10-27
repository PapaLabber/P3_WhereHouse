package group10;

import java.util.ArrayList;
import java.util.List;


public class Warehouse {
  private String name;
  private final double longitude;
  private final double latitude;
  private List<RealisedCapacity> realisedCapacities = new ArrayList<>();

  public Warehouse(String name, double longitude, double latitude) {
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

  public void addRealisedCapacity(RealisedCapacity rc) {
    realisedCapacities.add(rc);
  }

  public String setName(String name) {
    this.name = name;
  }

  public double getLongitude() {
    return this.longitude;
  }

  public double getLatitude() {
    return this.latitude;
  }

  public List<RealisedCapacity> getRealisedCapacities() {
    return realisedCapacities;
  }

  public String getName() {
    return this.name;
  }

}
