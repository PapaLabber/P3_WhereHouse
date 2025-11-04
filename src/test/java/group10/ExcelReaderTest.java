package group10;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
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

    // --- Helpers to create a tiny Excel file for each test ---

    private File makeWorkbook(String sheetName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(sheetName);
            // Header row (MUST match your reader’s expectations)
            Row header = sh.createRow(0);
            header.createCell(0).setCellValue("Country");
            header.createCell(1).setCellValue("PalletAmount");
            header.createCell(2).setCellValue("Date");
            header.createCell(3).setCellValue("Temperature");
            header.createCell(4).setCellValue("ProductionSite");

            // Let test-specific code add rows
            filler.fill(sh, wb);

            File f = tmp.resolve("test.xlsx").toFile();
            try (FileOutputStream out = new FileOutputStream(f)) {
                wb.write(out);
            }
            return f;
        }
    }

    // Create a native Excel date cell (numeric with date format)
    private void setExcelDate(Cell cell, Workbook wb, LocalDate date) {
        CreationHelper ch = wb.getCreationHelper();
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(ch.createDataFormat().getFormat("yyyy-mm-dd"));
        cell.setCellValue(java.util.Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
        cell.setCellStyle(dateStyle);
    }

    // Functional interface to fill rows for each test
    @FunctionalInterface
    interface RowFiller {
        void fill(Sheet sh, Workbook wb);
    }

    // ---------- Tests ----------

    @Test
    void filtersByCountryAndPallets_andParsesFields() throws Exception {
        // Arrange: two rows: one valid for DENMARK, one rejected (USA)
        File xlsx = makeWorkbook("Sheet1", (sh, wb) -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");          // Country
            r1.createCell(1).setCellValue(250);                 // PalletAmount
            setExcelDate(r1.createCell(2), wb, LocalDate.of(2025, 10, 28)); // Date
            r1.createCell(3).setCellValue("Cold");              // Temperature
            r1.createCell(4).setCellValue("Kalundborg");        // ProductionSite (known)

            Row r2 = sh.createRow(2);
            r2.createCell(0).setCellValue("USA");
            r2.createCell(1).setCellValue(500);
            setExcelDate(r2.createCell(2), wb, LocalDate.of(2025, 10, 28));
            r2.createCell(3).setCellValue("Ambient");
            r2.createCell(4).setCellValue("Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);

        // Act
        List<CapacityRequest> out = reader.filterRequest("DENMARK", 2025);

        // Assert
        assertEquals(1, out.size(), "Only Denmark row should pass and the year 2025 row should pass");
        CapacityRequest req = out.get(0);
        assertEquals(250, req.getPalletAmount());
        assertEquals(Temperature.COLD, req.getTemperature());
        assertEquals("Kalundborg", req.getProductionSite().getName());
    }

    @Test
    void skipsRowsWithPalletsZeroOrNegative() throws Exception {
        File xlsx = makeWorkbook("Sheet1", (sh, wb) -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(0);                   // <= 0 → should be skipped
            setExcelDate(r1.createCell(2), wb, LocalDate.of(2025, 1, 1));
            r1.createCell(3).setCellValue("Freeze");
            r1.createCell(4).setCellValue("Hillerød");

            Row r2 = sh.createRow(2);
            r2.createCell(0).setCellValue("Denmark");
            r2.createCell(1).setCellValue(-5);                  // <= 0 → should be skipped
            setExcelDate(r2.createCell(2), wb, LocalDate.of(2025, 1, 2));
            r2.createCell(3).setCellValue("Ambient");
            r2.createCell(4).setCellValue("Kalundborg");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "No rows should pass when pallets <= 0");
    }

    @Test
    void skipsUnknownProductionSite() throws Exception {
        File xlsx = makeWorkbook("Sheet1", (sh, wb) -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(100);
            setExcelDate(r1.createCell(2), wb, LocalDate.of(2025, 2, 2));
            r1.createCell(3).setCellValue("Ambient");
            r1.createCell(4).setCellValue("NonExistingSite"); // not in ProductionSite registry
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertEquals(0, out.size(), "Unknown site must be rejected");
    }

    @Test
    void skipsInvalidTemperature() throws Exception {
        File xlsx = makeWorkbook("Sheet1", (sh, wb) -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(100);
            setExcelDate(r1.createCell(2), wb, LocalDate.of(2025, 3, 3));
            r1.createCell(3).setCellValue("Hot"); // invalid temperature value
            r1.createCell(4).setCellValue("Hillerød");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "Invalid temperature should be rejected");
    }

    @Test
    void acceptsStringDateFormatAlso() throws Exception {
        File xlsx = makeWorkbook("Sheet1", (sh, wb) -> {
            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellValue("Denmark");
            r1.createCell(1).setCellValue(42);
            // Provide date as STRING (ISO), not as native Excel date
            r1.createCell(2).setCellValue("2025-12-24");
            r1.createCell(3).setCellValue("Freeze");
            r1.createCell(4).setCellValue("Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertEquals(1, out.size());
        assertEquals(Temperature.FREEZE, out.get(0).getTemperature());
        assertEquals("Måløv", out.get(0).getProductionSite().getName());
    }
}