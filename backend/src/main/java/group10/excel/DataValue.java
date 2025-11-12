package group10.excel;

/**
 * Minimal Value placeholder used by OutputResult.
 * Adjust fields/types to match actual OutputResult usage if needed.
 */
public class DataValue {
    private String key;
    private int amount;

    public DataValue() {}

    public DataValue(String key, int amount) {
        this.key = key;
        this.amount = amount;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "Value[key=" + key + ", amount=" + amount + "]";
    }
}