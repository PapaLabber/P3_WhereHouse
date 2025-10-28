package group10.excel;

import java.time.LocalDate;

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
