// package group10.excel;

// import java.io.File;
// import java.io.IOException;
// import java.text.SimpleDateFormat;

// import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
// import org.apache.poi.ss.usermodel.Cell;
// import org.apache.poi.ss.usermodel.DateUtil;
// import org.apache.poi.ss.usermodel.Row;
// import org.apache.poi.ss.usermodel.Sheet;
// import org.apache.poi.ss.usermodel.Workbook;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// public class ExcelReader {

// 	private Workbook workbook;
// 	private Sheet sheet;

// 	public ExcelReader(File excelFile) throws InvalidFormatException, IOException {
// 		workbook = new XSSFWorkbook(excelFile);
// 		sheet = workbook.getSheetAt(0);
// 	}

// 	public void readFromExcelFile() throws IOException {
// 		// First pass: calculate maximum width for each column
// 		int[] maxWidths = calculateColumnWidths();
		
// 		// Second pass: print with proper alignment
// 		for (Row row : sheet) {
// 			if (row == null) continue;
			
// 			// Iterate through ALL columns, not just non-blank ones
// 			int lastColumn = row.getLastCellNum();
// 			for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
// 				Cell cell = row.getCell(cellNum);
// 				String value = getCellValueAsString(cell);
// 				int width = maxWidths[cellNum];
// 				System.out.printf("%-" + width + "s  ", value);
// 			}
// 			System.out.println();
// 		}
		
// 		workbook.close();
// 	}
	
// 	private int[] calculateColumnWidths() {
// 		int maxColumns = 0;
		
// 		// Find max number of columns
// 		for (Row row : sheet) {
// 			if (row != null && row.getLastCellNum() > maxColumns) {
// 				maxColumns = row.getLastCellNum();
// 			}
// 		}
		
// 		int[] maxWidths = new int[maxColumns];
		
// 		// Calculate max width for each column
// 		for (Row row : sheet) {
// 			if (row == null) continue;
			
// 			int lastColumn = row.getLastCellNum();
// 			for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
// 				Cell cell = row.getCell(cellNum);
// 				String value = getCellValueAsString(cell);
// 				if (value.length() > maxWidths[cellNum]) {
// 					maxWidths[cellNum] = value.length();
// 				}
// 			}
// 		}
		
// 		return maxWidths;
// 	}
	
// 	private String getCellValueAsString(Cell cell) {
// 		// Handle null cells (blank cells)
// 		if (cell == null) {
// 			return "";
// 		}
		
// 		switch (cell.getCellType()) {
// 			case STRING:
// 				return cell.getStringCellValue();
// 			case NUMERIC: 
// 				if (DateUtil.isCellDateFormatted(cell)) {
// 					return new SimpleDateFormat("MM-dd-yyyy").format(cell.getDateCellValue());
// 				} else {
// 					return String.valueOf((int) cell.getNumericCellValue());
// 				}
// 			case BLANK:
// 				return "";
// 			default:
// 				return "";
// 		}
// 	}

// }

package group10.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
// import java.time.LocalDate;
// import java.time.ZoneId;
import java.util.*;

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
 *   "Date"
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

    public List<CapacityRequest> loadRequestsFilteredByCountry(String wantedCountry) throws IOException {
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

            // C. Parse ProductionSite using the registry
            String siteName = getStringCell(row, colIndex.get("ProductionSite"));
            ProductionSite site = ProductionSite.fromName(siteName);
            if (site == null) {
                System.err.println("Skipping row: unknown ProductionSite '" + siteName + "'");
                continue;
            }

            // D. Parse Temperature -> TemperatureZone enum
            String tempRaw = getStringCell(row, colIndex.get("Temperature"));
            Temperature zone = Temperature.fromString(tempRaw);
            if (zone == null) {
                System.err.println("Skipping row: invalid Temperature '" + tempRaw + "'");
                continue;
            }


            // F. Build domain object
            CapacityRequest req = new CapacityRequest(
                pallets,
                zone,
                site
            );

            // G. Keep it
            result.add(req);
        }

        workbook.close();
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

    /**
     * Read a date cell into a LocalDate.
     * Supports:
     *  - real Excel date cells (numeric + formatted as date)
     *  - string dates like "2025-10-28"
     *
     * Returns null if invalid.
     */
    // private LocalDate getDateCellAsLocalDate(Row row, Integer colIdx) {
    //     if (colIdx == null) return null;
    //     Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    //     if (cell == null) return null;

    //     // Case 1: Native Excel date (numeric serial)
    //     if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
    //         return cell.getDateCellValue()
    //             .toInstant()
    //             .atZone(ZoneId.systemDefault())
    //             .toLocalDate();
    //     }

    //     // Case 2: Text date like "2025-10-28"
    //     if (cell.getCellType() == CellType.STRING) {
    //         String raw = cell.getStringCellValue().trim();
    //         if (raw.isEmpty()) return null;
    //         try {
    //             return LocalDate.parse(raw); // ISO yyyy-MM-dd
    //         } catch (Exception e) {
    //             return null;
    //         }
    //     }

    //     return null;
    }
// }
