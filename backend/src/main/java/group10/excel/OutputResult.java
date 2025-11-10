package group10.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class OutputResult {
    @Value("${app.output-dir}")
    private String outputDirPath;

    private File createOutputFile(String sheetName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(sheetName);
            createOutputHeaderRow(sh);
            filler.fill(sh);

            File outputDir = new File(outputDirPath);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File f = new File(outputDir, "allocationResult.xlsx");

            try (FileOutputStream out = new FileOutputStream(f)) {
                wb.write(out);
            }
            return f;
        }
    }
    
    @FunctionalInterface
    interface RowFiller {
        void fill(Sheet sh);
    }

    void writeResultsToExcel(List<Result> results) throws IOException {
        createOutputFile("Allocation Results", sh -> {
            int rowIndex = 1; // Start from row 1 since row 0 is header
            for (Result result : results) {
                Row row = sh.createRow(rowIndex++);
                Cell warehouseCell = row.createCell(0);
                warehouseCell.setCellValue(result.getWarehouse().toString());

                Cell temperatureCell = row.createCell(1);
                temperatureCell.setCellValue(result.getTemperature().toString());

                Cell amountStoredCell = row.createCell(2);
                amountStoredCell.setCellValue(result.getAmountStored());
            }
        });
    }



    private void createOutputHeaderRow(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Warehouse");
        header.createCell(1).setCellValue("Storage Condition");
        header.createCell(2).setCellValue("Amount Stored");
        
    }
}
