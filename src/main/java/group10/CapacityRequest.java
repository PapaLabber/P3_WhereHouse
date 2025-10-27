package group10;

import java.time.LocalDate;
import group10.Condition;
import group10.ProductionSite;

public class CapacityRequest {
  private int palletAmount;
  private LocalDate date;

  public CapacityRequest(int palletAmount, LocalDate date, Condition condition, ProductionSite productionSite) {
    this.palletAmount = palletAmount;
    this.date = date;
    this.condition = condition;
    this.productionSite = productionSite;
  }


  public int getPalletAmount() {
    return this.palletAmount;
  }
  public LocalDate getDate() {
    return this.date;
  }
  public Condition getCondition() {
    return this.condition;
  }
  public ProductionSite getProductionSite() {
    return this.productionSite;
  }
}
