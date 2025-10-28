package group10.excel;

import java.time.LocalDate;

public class RealisedCapacity {
  private int palletAmount;
  private LocalDate date;
  private Condition condition;
  private Warehouse warehouse;

  public RealisedCapacity(int palletAmount, LocalDate date, Condition condition, Warehouse warehouse) {
    this.palletAmount = palletAmount;
    this.date = date; // Behøves dette eller skal vi bruge datoen?
    this.condition = condition;
    this.warehouse = warehouse;
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

  public Warehouse getWarehouse() {
    return this.warehouse;
  }

}
