package group10.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OutputResult {
    @Value("${app.output-dir}")
    private final String outputDirPath;

    public OutputResult(@Value("${app.output-dir:./outputFile}") String outputDirPath) {
        this.outputDirPath = (outputDirPath == null || outputDirPath.isBlank()) ? "./outputFile" : outputDirPath;
    }

    public OutputResult() {
        this("./outputFile");
    }

    private File createOutputFile(String sheetName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(sheetName);
            createOutputHeaderRow(sh);
            filler.fill(sh);

            File outputDir = new File(outputDirPath);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File f = new File(outputDir, sheetName);

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

    public Path writeResultsToExcel(List<Result> results, String fileName) throws IOException {
        if (!fileName.endsWith(".xlsx")) fileName += ".xlsx";
        Path outDir = Paths.get("./outputFile");
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve(fileName);
        
        createOutputFile(fileName, sh -> {
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
        return outFile;
    }

    private void createOutputHeaderRow(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Warehouse");
        header.createCell(1).setCellValue("Storage Condition");
        header.createCell(2).setCellValue("Amount Stored");

    }
}
