package group10.app;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import group10.excel.CapacityRequest;
import group10.excel.ExcelReader;
import group10.excel.RealizedCapacity;
import group10.excel.OutputResult;
import group10.excel.ProductionSite;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;

public class TestRunner {
    public static void main(String[] args) throws InvalidFormatException, IOException {
        // File excelFile = new File("CapacitydataMay2025.xlsx");
        // ExcelReader reader = new ExcelReader(excelFile);

        // String wantedCountry = "DENMARK"; // Skal ændres til at være dynamisk
        // int wantedYear = 2026; // Skal ændres til at være dynamisk

        // // Filter for Denmark and pallet > 0
        // List<CapacityRequest> wantedRequests = reader.filterRequest(wantedCountry, wantedYear);

        // for (CapacityRequest req : wantedRequests) {
        //     System.out.println(req);
        // }

        // List<RealizedCapacity> capacities = reader.warehouseCapacity(wantedCountry, wantedYear);

        // for (RealizedCapacity cap : capacities) {
        //     System.out.println(cap);
        // }

        List<Result> results = new ArrayList<>();
        Warehouse neff = new Warehouse("NEFF", 12, 55);
        Warehouse pspaci = new Warehouse("PS PAC I", 12, 55);
        Warehouse kalundborg = new Warehouse("PS WH KA", 12, 55);

        List<CapacityRequest> requests = new ArrayList<>();
        requests.add(new CapacityRequest(100, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));
        requests.add(new CapacityRequest(100, Temperature.COLD, ProductionSite.fromName("Kalundborg"), 2, 2026));
        requests.add(new CapacityRequest(100, Temperature.FREEZE, ProductionSite.fromName("Hjørring"), 3, 2026));


        Result result1 = new Result(neff, Temperature.AMBIENT, 100, requests.get(0));
        Result result2 = new Result(pspaci, Temperature.AMBIENT, 100, requests.get(1));
        Result result3 = new Result(kalundborg, Temperature.AMBIENT, 100, requests.get(2));

        results.add(result1);
        results.add(result2);
        results.add(result3);

        OutputResult output = new OutputResult("./outputFile");
        Path file = output.writeResultsToExcel(results, "AllocatedResult2027FRANCE");
        System.out.println("Wrote Excel file: " + file.toAbsolutePath());
    }
}
