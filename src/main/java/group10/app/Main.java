package group10.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import group10.excel.CapacityRequest;
import group10.excel.ExcelReader;


public class Main {
    public static void main(String[] args) throws InvalidFormatException, IOException {
        File excelFile = new File("CapacitydataMay2025.xlsx");
        ExcelReader reader = new ExcelReader(excelFile);

        // Filter for Denmark and pallet > 0
        List<CapacityRequest> wantedRequests = reader.filterRequest("DENMARK", 2025);

        for(CapacityRequest req : wantedRequests) {
            System.out.println(req);
        }
    }
}

