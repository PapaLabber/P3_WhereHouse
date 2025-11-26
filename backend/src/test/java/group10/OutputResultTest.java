package group10;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for OutputResult functionality
 * Tests writing of results to Excel files
 */
public class OutputResultTest {

    @TempDir
    Path tmp;

    @FunctionalInterface
    interface RowFiller {
        void fill(Sheet sh);
    }

    @Test
    void testFileCreation() throws IOException {
        File xlsx = makeWorkbook("sheet1", sh -> {
            Row row = sh.createRow(1); // Add a row after the header
            row.createCell(0).setCellValue("Test Warehouse");
            row.createCell(1).setCellValue("Ambient");
            row.createCell(2).setCellValue(100);
        });

        // Verify the file exists
        assertTrue(xlsx.exists(), "The Excel file should be created.");
        assertTrue(Files.size(xlsx.toPath()) > 0, "File should not be empty");
    }

    @Test
    void testExcelFileWriting() throws IOException {
        File xlsx = makeWorkbook("Sheet1", sh -> {
            Row row = sh.createRow(1);
            row.createCell(0).setCellValue("Test Warehouse");
            row.createCell(1).setCellValue("Ambient");
            row.createCell(2).setCellValue(100);
        });

        try (
            FileInputStream fileInputStream = new FileInputStream(xlsx);
            Workbook wb = new XSSFWorkbook(fileInputStream);
        ) {
            Sheet sheet = wb.getSheetAt(0);

            Row header = sheet.getRow(0);
            assertEquals("Warehouse", header.getCell(0).getStringCellValue());
            assertEquals("Storage Condition", header.getCell(1).getStringCellValue());
            assertEquals("Amount Stored", header.getCell(2).getStringCellValue());

            Row r1 = sheet.getRow(1);
            assertEquals("Test Warehouse", r1.getCell(0).getStringCellValue());
            assertEquals("Ambient", r1.getCell(1).getStringCellValue());
            assertEquals(100, r1.getCell(2).getNumericCellValue());

            assertEquals(1, sheet.getLastRowNum(), "Expected only 1 data row");
        }
    }

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

    private void createHeaderRow(Sheet sh) {
        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Warehouse");
        header.createCell(1).setCellValue("Storage Condition");
        header.createCell(2).setCellValue("Amount Stored");

    }

}
