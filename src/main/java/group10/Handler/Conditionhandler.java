package group10.Handler;

public class ConditionHandler {
  public static void handle(String condition) {
    String lower = condition.toLowerCase();

    // Mangler et par conditions
    if (lower.contains("crt") || lower.contains("15°") || lower.contains("25°")) {
      System.out.println("Room Temperature");
    } else if (lower.contains("2°") || lower.contains("8°") || lower.contains("cold")) {
      System.out.println("Refrigerated");
    } else if (lower.contains("below") || lower.contains("0°") || lower.contains("frozen")) {
      System.out.println("Frozen");
    } else {
      System.out.println("Ukendt (" + condition + ")");
    }
  }
}
