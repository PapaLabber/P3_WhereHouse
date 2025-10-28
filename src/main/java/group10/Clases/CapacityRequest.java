package group10.Clases;

import java.time.LocalDate;

import group10.Clases.Condition;
import group10.Clases.ProductionSite;

public class CapacityRequest {
  private int palletAmount;
  private LocalDate date;
  private Condition condition;
  private ProductionSite productionSite;

  public CapacityRequest(int palletAmount, LocalDate date, Condition condition, ProductionSite productionSite) {
    this.palletAmount = palletAmount;
    this.date = date;
    this.condition = condition;
    this.productionSite = productionSite;
  }
}
