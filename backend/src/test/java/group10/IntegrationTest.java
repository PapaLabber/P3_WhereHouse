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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import group10.algorithms.WarehouseAllocator;
import group10.excel.CapacityRequest;
import group10.excel.ExcelReader;
import group10.excel.OutputResult;
import group10.excel.RealizedCapacity;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;

class IntegrationTest {
    /**
     * Integration tests that exercise the full flow: ExcelReader -> WarehouseAllocator -> OutputResult.
     *
     * This test builds an in-memory Excel workbook (written to a JUnit temporary directory), uses
     * the ExcelReader to parse requests and capacities, runs the allocator to produce allocation 
     * results, and finally writes those results to an Excel file, which the test verifies. 
     */
    
    @TempDir // JUnit temporary directory; files created here are cleaned up automatically.
    Path tmp;
    
    // Helper that creates a simple .xlsx workbook in the test temporary directory.
    private File makeWorkbook(String fileName, RowFiller filler) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("Sheet1");
            createHeaderRow(sh);
            filler.fill(sh);

            File f = tmp.resolve(fileName).toFile();
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
    
    // Creates the sheet header. Column names must match the keys used by `ExcelReader`
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

    // Creates a data row with the commonly used columns: country, pallets,
    // year, temperature. Subsequent helper methods fill the remaining columns.
    private Row createCommonFields(Sheet sh, int rowNum, String country, int pallets, int year, String temperature) {
        Row row = sh.createRow(rowNum);
        row.createCell(0).setCellValue(country);
        row.createCell(1).setCellValue(pallets);
        row.createCell(2).setCellValue(year);
        row.createCell(3).setCellValue(temperature);
        return row;
    }
    
    // Add a CapacityRequest row.
    private void createRequestRow(Sheet sh, int rowNum, String country, int pallets, int year, String temperature, String productionSite) {
        Row row = createCommonFields(sh, rowNum, country, pallets, year, temperature);
        row.createCell(4).setCellValue(productionSite);
    }

    // Add a RealizedCapacity row.
    private void createCapacityRow(Sheet sh, int rowNum, String country, int pallets, int year, String temperature, String warehouse, int capacity) {
        Row row = createCommonFields(sh, rowNum, country, pallets, year, temperature);
        row.createCell(4).setCellValue("");
        row.createCell(5).setCellValue(warehouse);
        row.createCell(6).setCellValue(capacity);
    }

    /**
     * Integration test that demonstrates the expected usage pattern:
     *
     * Steps: 1) Create a small Excel file with one request and one realized capacity 2)
     * Use `ExcelReader` to parse requests and realized capacities 3) Run
     * `WarehouseAllocator` and assert the allocation is as expected 4) Use
     * `OutputResult` to write the allocation to disk and validate the file
     *
     * Each step has an assertion to serve as an incremental waypoint so
     * failures indicate which component or contract changed.
     */
    @Test
    void integration_workflow_excel_to_allocator_to_output() throws Exception {
        // 1) Create a simple workbook with one request and one warehouse (same temperature)
        File integrationTestFile = makeWorkbook("integration_test_file.xlsx", sh -> {
            // add a request row (country, pallets, year, temperature, site)
            createRequestRow(sh, 1, "Denmark", 80, 2026, "Ambient", "Hillerød");
            // add a realized capacity row (country, year, temperature, warehouse, capacity)
            createCapacityRow(sh, 2, "Denmark", 0, 2026, "Ambient", "PS PAC I", 100);
        });

        // 2) Use ExcelReader to parse the file — this validates the reader functionality
        ExcelReader reader = new ExcelReader(integrationTestFile);

        // `filterRequest` returns CapacityRequest rows filtered by country+year and
        // converts them into domain objects (pallet amount, temperature, site, id, year).
        // We use uppercase country here because the reader matches case-insensitively.
        List<CapacityRequest> requests = reader.filterRequest("DENMARK", 2026);

        // `getRealizedCap` scans the same sheet and returns RealizedCapacity objects
        // (warehouse, capacity, temperature, year). These are the supply side for the allocator.
        List<RealizedCapacity> capacities = reader.getRealizedCap("DENMARK", 2026);

        // Small checkpoints: ensure reader returned both request and capacity
        assertEquals(1, requests.size(), "Expected exactly one capacity request parsed from Excel");
        assertEquals(1, capacities.size(), "Expected exactly one realized capacity parsed from Excel");

        // Verify parsed field values to catch parsing regressions early.
        // These assertions make it obvious if the ExcelReader mapping changes.
        CapacityRequest req = requests.get(0);
        assertEquals(80, req.getPalletAmount(), "Parsed pallet amount should match the workbook");
        assertEquals(Temperature.AMBIENT, req.getTemperature(), "Parsed temperature should match the workbook");

        RealizedCapacity cap = capacities.get(0);
        assertEquals(100, cap.getPalletAmount(), "Parsed warehouse capacity should match the workbook");
        assertEquals(Temperature.AMBIENT, cap.getTemperature(), "Parsed warehouse temperature should match the workbook");

        // 3) Run the allocator using the parsed domain objects
        // The allocator implements the OR-Tools model and returns a list of Result objects.
        // Each Result links a Warehouse with a CapacityRequest,
        // the temperature condition and the assigned pallet amount.
        WarehouseAllocator allocator = new WarehouseAllocator();
        List<Result> results = allocator.Allocator(requests, capacities);

        // Check that the allocator returns the expected single allocation.
        // We compare the `toString()` values because the domain classes currently
        // don't override `equals()` for deep field comparison in tests.
        assertEquals(1, results.size(), "Allocator should produce one allocation result");
        Result r = results.get(0);
        Result expected = new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0));
        assertEquals(expected.toString(), r.toString(), "Allocator output should match expected allocation");

        // 4) Persist results to an Excel file using OutputResult and verify the written file.
        // `writeResultsToExcel` returns the `Path` of the created file; it will append
        // the `.xlsx` suffix if missing and create the `./outputFile` directory if needed.
        OutputResult output = new OutputResult();
        Path outPath = output.writeResultsToExcel(results, "IntegrationOut.xlsx");
        assertNotNull(outPath, "Output path should not be null");
        File outFile = outPath.toFile();
        assertTrue(outFile.exists(), "Written output file should exist");

        // Quick content check: header names and the numeric stored amount
        try (Workbook wb = new XSSFWorkbook(outFile)) {
            Sheet sh = wb.getSheetAt(0);
            Row header = sh.getRow(0);
            assertEquals("Warehouse", header.getCell(0).getStringCellValue());
            assertEquals("Storage Condition", header.getCell(1).getStringCellValue());
            assertEquals("Amount Stored", header.getCell(2).getStringCellValue());

            // Inspect the first data row written by OutputResult:
            Row data = sh.getRow(1);
            String warehouseCell = data.getCell(0).getStringCellValue();
            // OutputResult writes the warehouse using `toString()`; assert the name is present
            assertTrue(warehouseCell.contains("PS PAC I"), "Warehouse cell should contain warehouse name");
            // The amount is written as a numeric cell in column index 2
            double amount = data.getCell(2).getNumericCellValue();
            assertEquals(80.0, amount, "Amount stored cell should equal allocated pallets");
        }
    }
}
