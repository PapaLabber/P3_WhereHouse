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
import group10.excel.RealisedCapacity;
import group10.excel.Temperature;

/**
 * Test class for ExcelReader functionality
 * Tests reading and filtering of warehouse capacity and production site
 * requests
 */
class ExcelReaderTest {
    @TempDir
    Path tmp;

    /**
     * Creates a test workbook with given sheet name and content
     * 
     * @param sheetName Name of the sheet to create
     * @param filler    Function to fill the sheet with data
     * @return File object containing the created Excel workbook
     */
    private File makeWorkbook(String sheetName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet(sheetName);
            createHeaderRow(sh);
            filler.fill(sh);

            File f = tmp.resolve("test.xlsx").toFile();
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

    /**
     * Tests filtering of requests by country and year while validating field
     * parsing
     */
    @Test
    void filtersByCountryPalletsYear_andParsesFields() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createRequestRow(sh, 1, "Denmark", 250, 2025, "Cold", "Kalundborg");
            createRequestRow(sh, 2, "USA", 500, 2025, "Ambient", "Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("DENMARK", 2025);

        assertEquals(1, out.size(), "Only Denmark 2025 row should pass");
        CapacityRequest req = out.get(0);
        assertEquals(250, req.getPalletAmount());
        assertEquals(Temperature.COLD, req.getTemperature());
        assertEquals("Kalundborg", req.getProductionSite().getName());
        assertEquals(2025, req.getYear());
    }

    /**
     * Tests that rows with zero or negative pallet amounts are skipped
     */
    @Test
    void skipsRowsWithPalletsZeroOrNegative() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createRequestRow(sh, 1, "Denmark", 0, 2025, "Freeze", "Hillerød");
            createRequestRow(sh, 2, "Denmark", -5, 2025, "Ambient", "Kalundborg");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "No rows should pass when pallets <= 0");
    }

    /**
     * Tests that rows with invalid temperature values are skipped
     */
    @Test
    void skipsInvalidTemperature() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createRequestRow(sh, 1, "Denmark", 100, 2025, "Hot", "Hillerød");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("Denmark", 2025);
        assertTrue(out.isEmpty(), "Invalid temperature should be rejected");
    }

    /**
     * Tests case-insensitive matching for country and temperature fields
     */
    @Test
    void acceptsMixedCaseCountryAndTemperature() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createRequestRow(sh, 1, "denMARK", 42, 2025, "fReeZe", "Måløv");
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<CapacityRequest> out = reader.filterRequest("DENMARK", 2025);
        assertEquals(1, out.size());
        assertEquals(Temperature.FREEZE, out.get(0).getTemperature());
        assertEquals(2025, out.get(0).getYear());
    }

    /**
     * Tests filtering of warehouse capacity by year and country while validating
     * field parsing
     */
    @Test
    void filtersByYearAndCountryAndParsesFields() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createWarehouseRow(sh, 1, "Denmark", 0, 2025, "Ambient", "PS PAC I", 20000);
            createWarehouseRow(sh, 2, "USA", 0, 2025, "Ambient", "KN Durham 2", 20000);
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<RealisedCapacity> output = reader.warehouseCapacity("DENMARK", 2025);

        assertEquals(1, output.size(), "Only Denmark 2025 row should pass");
        RealisedCapacity req = output.get(0);
        assertEquals(20000, req.getPalletAmount());
        assertEquals(Temperature.AMBIENT, req.getTemperature());
        assertEquals("PS PAC I", req.getWarehouse().getName());
        assertEquals(2025, req.getYear());
    }

    /**
     * Tests that warehouse rows with zero or negative capacity are skipped
     */
    @Test
    void skipsRowsWithRealisedCapacityZeroOrNegative() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createWarehouseRow(sh, 1, "Denmark", 0, 2025, "Ambient", "PS PAC I", 0);
            createWarehouseRow(sh, 2, "DENMARK", 0, 2025, "Ambient", "KN Durham 2", -5);
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<RealisedCapacity> out = reader.warehouseCapacity("Denmark", 2025);
        assertTrue(out.isEmpty(), "No rows should pass when realised capacity <= 0");
    }

    /**
     * Tests that warehouse rows with invalid temperature values are skipped
     */
    @Test
    void skipsInvalidTemperatureForWarehouse() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createWarehouseRow(sh, 1, "Denmark", 0, 2025, "HOT", "PS PAC I", 20000);
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<RealisedCapacity> out = reader.warehouseCapacity("Denmark", 2025);
        assertTrue(out.isEmpty(), "Invalid temperature should be rejected");
    }

    /**
     * Tests case-insensitive matching for country and temperature in warehouse data
     */
    @Test
    void acceptsMixedCaseCountryAndTemperatureForWarehouse() throws Exception {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            createWarehouseRow(sh, 1, "DenMaRK", 0, 2025, "aMBienT", "PS PAC I", 20000);
        });

        ExcelReader reader = new ExcelReader(xlsx);
        List<RealisedCapacity> out = reader.warehouseCapacity("DENMARK", 2025);
        assertEquals(1, out.size());
        assertEquals(Temperature.AMBIENT, out.get(0).getTemperature());
        assertEquals(2025, out.get(0).getYear());
    }

    // Helper Functions

    /**
     * Creates the header row with standard column names
     * 
     * @param sh Sheet to add header row to
     */
    private void createHeaderRow(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Country");
        header.createCell(1).setCellValue("PalletAmount");
        header.createCell(2).setCellValue("Year");
        header.createCell(3).setCellValue("Temperature");
        header.createCell(4).setCellValue("ProductionSite");
        header.createCell(5).setCellValue("Warehouse");
        header.createCell(6).setCellValue("L&D Capacity (Physical pallet spaces)");
    }

    /**
     * Creates a row with common fields (country, pallets, year, temperature)
     * 
     * @param sh          Sheet to add row to
     * @param rowNum      Row number
     * @param country     Country name
     * @param pallets     Number of pallets
     * @param year        Year value
     * @param temperature Temperature category
     * @return Created row with common fields
     */
    private Row createCommonFields(Sheet sh, int rowNum, String country, int pallets, int year, String temperature) {
        Row row = sh.createRow(rowNum);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(pallets);
        row.createCell(2).setCellValue(year);
        row.createCell(3).setCellValue(temperature);
        return row;
    }

    /**
     * Creates a complete capacity request row
     * 
     * @param sh             Sheet to add row to
     * @param rowNum         Row number
     * @param country        Country name
     * @param pallets        Number of pallets
     * @param year           Year value
     * @param temperature    Temperature category
     * @param productionSite Production site name
     */
    private void createRequestRow(Sheet sh, int rowNum, String country, int pallets,
            int year, String temperature, String productionSite) {
        Row row = createCommonFields(sh, rowNum, country, pallets, year, temperature);
        row.createCell(4).setCellValue(productionSite);
    }

    /**
     * Creates a complete warehouse capacity row
     * 
     * @param sh          Sheet to add row to
     * @param rowNum      Row number
     * @param country     Country name
     * @param pallets     Number of pallets
     * @param year        Year value
     * @param temperature Temperature category
     * @param warehouse   Warehouse name
     * @param capacity    Warehouse capacity
     */
    private void createWarehouseRow(Sheet sh, int rowNum, String country, int pallets,
            int year, String temperature, String warehouse, int capacity) {
        Row row = createCommonFields(sh, rowNum, country, pallets, year, temperature);
        row.createCell(4).setCellValue(""); // ProductionSite empty for warehouse
        row.createCell(5).setCellValue(warehouse);
        row.createCell(6).setCellValue(capacity);
    }
}