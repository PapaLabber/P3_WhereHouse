package group10.app;

import group10.excel.ExcelReader;
import group10.excel.CapacityRequest;
import java.util.List;


import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


public class Main {
    public static void main(String[] args) throws InvalidFormatException, IOException {
        File excelFile = new File("CapacitydataMay2025.xlsx");
        ExcelReader reader = new ExcelReader(excelFile);

        // Filter for Denmark and pallet > 0
        List<CapacityRequest> denmarkRequests = reader.loadRequestsFilteredByCountry("DENMARK");

        // Print results
        for (CapacityRequest req : denmarkRequests) {
            System.out.println(req);
        }
    }
}

