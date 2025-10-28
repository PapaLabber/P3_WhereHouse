package group10.Handler;

public class TemperatureHandler {
  public static void handle(String Temperature) {
    if (Temperature == null) return;

    String lower = Temperature.toLowerCase().trim();

    switch (lower) {
      case "ambient":
        System.out.println("Room Temperature");
        break;
      case "cold":
        System.out.println("Refrigerated");
        break;
      case "freeze":
        System.out.println("Frozen");
        break;
      default:
        System.out.println("Ukendt (" + Temperature + ")");
        break;
    }
  }
}
