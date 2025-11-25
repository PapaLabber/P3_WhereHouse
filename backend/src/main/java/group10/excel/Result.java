package group10.excel;

public class Result {
    private Warehouse warehouse;
    private Temperature temperature;
    private int amountStored;
    private CapacityRequest request;

    public Result(Warehouse warehouse, Temperature temperature, int amountStored, CapacityRequest request) {
        this.warehouse = warehouse;
        this.temperature = temperature;
        this.amountStored = amountStored;
        this.request = request;
    }

    public Warehouse getWarehouse() {
        return this.warehouse;
    }

    public Temperature getTemperature() {
        return this.temperature;
    }

    public int getAmountStored() {
        return this.amountStored;
    }

    public CapacityRequest getRequest() {
        return this.request;
    }

    @Override
    public String toString() {
        return "Result [warehouse=" + this.warehouse + ", temperature=" + this.temperature + ", amountStored=" + this.amountStored + ", requestID=" + this.request.getID() + "]";
    }

    
}