
package group10.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reads the first sheet of an Excel file and returns all valid CapacityRequest rows.
 *
 * A row is considered valid if:
 *  - Country matches wantedCountry (case-insensitive)
 *  - PalletAmount > 0
 *  - ProductionSite is known (ProductionSite.fromName(...) returns non-null)
 *  - Temperature can be mapped to a TemperatureZone (Ambient / Cold / Freeze)
 *
 * Required columns in the Excel header row:
 *   "Country"
 *   "PalletAmount"
 *   "Temperature"
 *   "ProductionSite"
 */
public class ExcelReader {

    private final Workbook workbook;
    private final Sheet sheet;

    public ExcelReader(File excelFile) throws InvalidFormatException, IOException {
        this.workbook = new XSSFWorkbook(excelFile); // open .xlsx
        this.sheet = workbook.getSheetAt(0);         // first sheet only
    }

    public List<CapacityRequest> loadRequestsFilteredByCountry(String wantedCountry, int wantedYear) throws IOException {
        Iterator<Row> it = sheet.iterator();
        if (!it.hasNext()) { // no rows at all
            workbook.close();
            return Collections.emptyList();
        }

        // 1. Read header row, build lookup map: header name -> column index
        Row headerRow = it.next();
        Map<String, Integer> colIndex = getHeaderIndexMap(headerRow);

        List<CapacityRequest> result = new ArrayList<>();

        // 2. Process all data rows
        while (it.hasNext()) {
            Row row = it.next();
            if (row == null) continue;

            // A. Filter by Country
            String country = getStringCell(row, colIndex.get("Country"));
            if (country == null || !wantedCountry.equalsIgnoreCase(country.trim())) {
                continue;
            }

            // B. Filter by PalletAmount > 0
            int pallets = getIntCell(row, colIndex.get("PalletAmount"));
            if (pallets <= 0) {
                continue;
            }
        
            int year = getIntCell(row, colIndex.get("Year"));
            if(wantedYear != year) {
                continue;
            } 

            // C. Parse ProductionSite using the registry
            String siteName = getStringCell(row, colIndex.get("ProductionSite"));
            ProductionSite site = ProductionSite.fromName(siteName);

            // D. Parse Temperature -> TemperatureZone enum
            String tempRaw = getStringCell(row, colIndex.get("Temperature"));
            Temperature zone = Temperature.fromString(tempRaw);
            if (zone == null) {
                System.err.println("Skipping row: invalid Temperature '" + tempRaw + "'");
                continue;
            }

            // E. Compute ID from row number (make 1-based if desired)
            int ID = row.getRowNum() + 1;

            // F. Build domain object
            CapacityRequest req = new CapacityRequest(
                pallets,
                zone,
                site,
                ID,
                year
            );

            // G. Keep it
            result.add(req);

        workbook.close();
        }
        return result;
    }

    // ---------- helper methods below ----------

    /**
     * Build map of header name -> column index.
     * If header row has cells: [Country][PalletAmount][Date]...
     * You'll get { "Country"=0, "PalletAmount"=1, "Date"=2, ... }
     */
    private Map<String, Integer> getHeaderIndexMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerName = cell.getStringCellValue().trim();
            map.put(headerName, cell.getColumnIndex());
        }
        return map;
    }

    /**
     * Safely read a cell as String, even if the cell is numeric or boolean.
     * Returns null if the cell is blank or unusable.
     */
    private String getStringCell(Row row, Integer colIdx) {
        if (colIdx == null) return null; // header missing
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                double n = cell.getNumericCellValue();
                if (n == Math.floor(n)) {
                    return String.valueOf((long) n); // "42"
                } else {
                    return String.valueOf(n);        // "42.5"
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return null;
        }
    }

    /**
     * Read an integer-like value from a cell.
     * Returns 0 if blank or non-numeric.
     */
    private int getIntCell(Row row, Integer colIdx) {
        if (colIdx == null) return 0;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();

            case STRING:
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty()) return 0;
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return 0;
                }

            default:
                return 0;
        }
    
    }

}