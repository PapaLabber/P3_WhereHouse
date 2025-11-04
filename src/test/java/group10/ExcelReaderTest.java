package group10;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import group10.excel.CapacityRequest;
import group10.excel.ExcelReader;
import group10.excel.Temperature;

class ExcelReaderTest {

    @TempDir
    Path tmp;

    // ---- helper to create a tiny workbook per test ----
    private File makeWorkbook(String sheetName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(sheetName);

            // Header row MUST match the reader’s expectations
            Row header = sh.createRow(0);
            header.createCell(0).setCellValue("Country");
            header.createCell(1).setCellValue("PalletAmount");
            header.createCell(2).setCellValue("Year");
            header.createCell(3).setCellValue("Temperature");
            header.createCell(4).setCellValue("ProductionSite");

            // custom rows
            filler.fill(sh);

            File f = tmp.resolve("test.xlsx").toFile();
            try (FileOutputStream out = new FileOutputStream(f)) {
                wb.write(out);
            }
            return f;
        }
    }

    @FunctionalInterface
    interface RowFiller { void fill(Sheet sh); }

    @Test
    void filtersByCountryPalletsYear_andParsesFields() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");    // Country
            r1.createCell(1).setCellValue(250);          // PalletAmount
            r1.createCell(2).setCellValue(2025);         // Year (numeric)
            r1.createCell(3).setCellValue("Cold");       // Temperature
            r1.createCell(4).setCellValue("Kalundborg"); // ProductionSite

            Row r2 = sh.createRow(2);                    // should be filtered out (USA)
            r2.createCell(0).setCellValue("USA");
            r2.createCell(1).setCellValue(500);
            r2.createCell(2).setCellValue(2025);
            r2.createCell(3).setCellValue("Ambient");
            r2.createCell(4).setCellValue("Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("DENMARK", 2025);

        assertEquals(1, out.size(), "Only Denmark 2025 row should pass");
        CapacityRequest req = out.get(0);
        assertEquals(250, req.getPalletAmount());
        assertEquals(Temperature.COLD, req.getTemperature());
        assertEquals("Kalundborg", req.getProductionSite().getName());
        assertEquals(2025, req.getYear()); // ensure your model exposes getYear()
    }

    @Test
    void skipsRowsWithPalletsZeroOrNegative() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(0);            // <= 0 → skip
            r1.createCell(2).setCellValue(2025);
            r1.createCell(3).setCellValue("Freeze");
            r1.createCell(4).setCellValue("Hillerød");

            Row r2 = sh.createRow(2);
            r2.createCell(0).setCellValue("Denmark");
            r2.createCell(1).setCellValue(-5);           // <= 0 → skip
            r2.createCell(2).setCellValue(2025);
            r2.createCell(3).setCellValue("Ambient");
            r2.createCell(4).setCellValue("Kalundborg");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "No rows should pass when pallets <= 0");
    }

    @Test
    void skipsInvalidTemperature() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(100);
            r1.createCell(2).setCellValue(2025);
            r1.createCell(3).setCellValue("Hot"); // invalid temperature value
            r1.createCell(4).setCellValue("Hillerød");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "Invalid temperature should be rejected");
    }

    @Test
    void acceptsMixedCaseCountryAndTemperature() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("denMARK");    // mixed case
            r1.createCell(1).setCellValue(42);
            r1.createCell(2).setCellValue(2025);
            r1.createCell(3).setCellValue("fReeZe");     // mixed case
            r1.createCell(4).setCellValue("Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("DENMARK", 2025);
        assertEquals(1, out.size());
        assertEquals(Temperature.FREEZE, out.get(0).getTemperature());
        assertEquals(2025, out.get(0).getYear());
    }
}