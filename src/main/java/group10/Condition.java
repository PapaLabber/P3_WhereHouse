package group10;

public class Condition {
  private String type;

  /*
   * Måske bør vi overveje at sammenligne og tænke over,
   * hvordan vi kan inkorporere, at nogle værdier
   * kan ligge i intervallet -5 til 35 samt 15 til 25.
   */
  public Condition(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
