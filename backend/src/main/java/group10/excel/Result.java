public class Result {
    private Warehouse warehouse;
    private Temperature temperature;
    private int amountStored;

    public Result(Warehouse warehouse, Temperature temperature, int amountStored) {
        this.warehouse = warehouse;
        this.temperature = temperature;
        this.amountStored = amountStored;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public int getAmountStored() {
        return amountStored;
    }

    @Override
    public String toString() {
        return "Result [warehouse=" + warehouse + ", temperature=" + temperature + ", amountStored=" + amountStored + "]";
    }

    
}
