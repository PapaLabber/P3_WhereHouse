package group10.app;

import group10.excel.ExcelReader;
import group10.excel.CapacityRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


public class Main {
    public static void main(String[] args) throws InvalidFormatException, IOException {
        File excelFile = new File("CapacitydataMay2025.xlsx");
        ExcelReader reader = new ExcelReader(excelFile);

        String wantedCountry = "DENMARK"; // Skal ændres til at være dynamisk
        int wantedYear = 2025;            // Skal ændres til at være dynamisk

        // Filter for Denmark and pallet > 0
        List<CapacityRequest> wantedRequests = reader.filterRequest(wantedCountry, wantedYear);

        for(CapacityRequest req : wantedRequests) {
            System.out.println(req);
        }
    }
}