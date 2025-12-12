package group10.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reader for the Excel input file.
 *
 * Expects a sheet with at least the following columns:
 * - "Country"
 * - "PalletAmount"
 * - "Year"
 * - "ProductionSite"
 * - "Temperature"
 * - "ID" (for requests)
 * - "Warehouse"
 * - "Capacity"
 *
 * Adjust column names here if your file uses different labels.
 */
public class ExcelReader {

  private final Workbook workbook;
  private final Sheet sheet;

  public ExcelReader(File excelFile) throws InvalidFormatException, IOException {
    this.workbook = new XSSFWorkbook(excelFile); // Opens .xlsx file
    this.sheet = workbook.getSheetAt(0); // Uses first sheet
  }

  /**
   * Filters all CapacityRequest rows by country and year.
   *
   * Uses the following columns:
   * - "Country"
   * - "PalletAmount"
   * - "Year"
   * - "ProductionSite"
   * - "Temperature"
   * - "ID" (If used. Not necessary)
   *
   * @param wantedCountry filter by this country
   * @param wantedYear    filter by this year
   * @return list of valid CapacityRequest objects
   * @throws IOException if workbook cannot be read
   */
  public List<CapacityRequest> filterRequest(String wantedCountry, int wantedYear) throws IOException {
    Iterator<Row> it = sheet.iterator();
    if (!it.hasNext()) { // No rows
      workbook.close();
      return Collections.emptyList();
    }

    // Read header row and build mapping: header -> column index
    Row headerRow = it.next();
    Map<String, Integer> colIndex = getHeaderIndexMap(headerRow); // TODO: kom tilbage (Mads)

    List<CapacityRequest> result = new ArrayList<>();

    // Iterate through all data rows
    while (it.hasNext()) {
      Row row = it.next();
      if (row == null) {
        continue;
      }

      // Country
      String country = getStringCell(row, colIndex.get("Country"));
      if (country == null || !wantedCountry.equalsIgnoreCase(country.trim())) {
        continue;
      }

      // PalletAmount > 0
      int pallets = getIntCell(row, colIndex.get("PalletAmount"));
      if (pallets <= 0) {
        continue;
      }

      // Year
      int year = getIntCell(row, colIndex.get("Year"));
      if (wantedYear != year) {
        continue;
      }

      // ProductionSite
      String siteName = getStringCell(row, colIndex.get("ProductionSite"));
      ProductionSite site = ProductionSite.fromName(siteName);

      // Temperature
      String tempRaw = getStringCell(row, colIndex.get("Temperature"));
      Temperature zone = Temperature.fromString(tempRaw);
      if (zone == null) {
        System.err.println("Skipping row: invalid Temperature '" + tempRaw + "'");
        continue;
      }

      // ID (uses column if present, otherwise fallback to row number)
      int id;
      if (colIndex.containsKey("ID")) {
        id = getIntCell(row, colIndex.get("ID"));
      } else {
        id = row.getRowNum();
      }

      // Build CapacityRequest object
      CapacityRequest req = new CapacityRequest(
          pallets,
          zone,
          site,
          id,
          year);

      result.add(req);
    }
    // Close workbook after processing
    workbook.close();
    return result;
  }

  /**
   * Reads all RealizedCapacity rows from the sheet and filters by country and
   * year.
   * 
   * Uses the following columns:
   * - "Warehouse"
   * - "Capacity"
   * - "Temperature"
   * - "Year"
   *
   * @param wantedCountry filter by this country
   * @param wantedYear    filter by this year
   * @return list of RealizedCapacity objects for the given filters
   * @throws IOException if the workbook cannot be read
   */
  public List<RealizedCapacity> getRealizedCap(String wantedCountry, int wantedYear) throws IOException {
    Iterator<Row> it = sheet.iterator();
    if (!it.hasNext()) { // No rows available
      workbook.close();
      return Collections.emptyList();
    }

    // Read header row and create header -> index mapping
    Row headerRow = it.next();
    Map<String, Integer> colIndex = getHeaderIndexMap(headerRow);

    List<RealizedCapacity> result = new ArrayList<>();

    // Loop through all rows in the sheet
    while (it.hasNext()) {
      Row row = it.next();
      if (row == null) {
        continue;
      }

      // Country
      String country = getStringCell(row, colIndex.get("Country"));
      if (country == null || !wantedCountry.equalsIgnoreCase(country.trim())) {
        continue;
      }

      // Skip FP warehouses
      Set<String> SKIP_WAREHOUSES = new HashSet<>(Arrays.asList("dsv", "ps hub"));

      String warehouseCell = getStringCell(row, colIndex.get("Warehouse"));
      if (warehouseCell != null && SKIP_WAREHOUSES.contains(warehouseCell.trim().toLowerCase(Locale.ROOT))) {
        continue;
      }

      // PalletAmount > 0
      int pallets = getIntCell(row, colIndex.get("L&D Capacity (Physical pallet spaces)"));
      if (pallets <= 0) {
        continue;
      }

      // Skip years that are not specified
      int year = getIntCell(row, colIndex.get("Year"));
      if (wantedYear != year) {
        continue;
      }

      // Warehouse lookup
      String warehouseName = getStringCell(row, colIndex.get("Warehouse"));
      Warehouse warehouse = Warehouse.fromName(warehouseName);

      // Temperature lookup
      String tempRaw = getStringCell(row, colIndex.get("Temperature"));
      Temperature zone = Temperature.fromString(tempRaw);
      if (zone == null) {
        System.err.println("Skipping row: invalid Temperature '" + tempRaw + "'");
        continue;
      }

      // Build domain object
      RealizedCapacity req = new RealizedCapacity(
          pallets,
          zone,
          warehouse,
          year);

      result.add(req);
    }
    // Close workbook after reading
    workbook.close();
    return result;
  }

  /**
   * Helper used for tests.
   * Returns RealizedCapacity filtered by country and year.
   */

  // TODO
  public List<RealizedCapacity> warehouseCapacity(String wantedCountry, int wantedYear) throws IOException {
    // Get all and apply additional filtering
    List<RealizedCapacity> all = getRealizedCap(wantedCountry, wantedYear);
    List<RealizedCapacity> filtered = new ArrayList<>();

    for (RealizedCapacity rc : all) {
      // Year filter
      if (rc.getYear() != wantedYear) {
        continue;
      }

      Warehouse wh = rc.getWarehouse();
      // Hvis Warehouse ikke har country, så fjern dette filter.
      // if (!wh.getCountry().equalsIgnoreCase(wantedCountry)) {
      // continue;
      // }

      filtered.add(rc);
    }

    return filtered;
  }

  /**
   * Creates a map of headerName -> column index from the header row.
   */
  private Map<String, Integer> getHeaderIndexMap(Row headerRow) {
    Map<String, Integer> map = new HashMap<>();
    for (Cell cell : headerRow) {
      if (cell == null)
        continue;
      if (cell.getCellType() != CellType.STRING)
        continue;
      String headerName = cell.getStringCellValue().trim();
      map.put(headerName, cell.getColumnIndex());
    }
    return map;
  }

  /**
   * Reads a cell as a String.
   * Returns null if empty.
   */

  //TODO
  private String getStringCell(Row row, Integer colIdx) {
    if (colIdx == null) {
      return null; // header mangler
    }
    Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
      return null;
    }

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();

      case NUMERIC:
        double n = cell.getNumericCellValue();
        if (n == Math.floor(n)) {
          return String.valueOf((long) n);
        } else {
          return String.valueOf(n);
        }

      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());

      default:
        return null;
    }
  }

  /**
   * Reads an integer from a cell.
   * Returns 0 if blank or not numeric.
   */

  // TODO
  private int getIntCell(Row row, Integer colIdx) { // TODO: sammenlign med getStringCell (Erik)
    if (colIdx == null) {
      return 0;
    }
    Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
    if (cell == null) {
      return 0;
    }

    switch (cell.getCellType()) {
      case NUMERIC:
        return (int) Math.round(cell.getNumericCellValue());
      case STRING:
        try {
          return Integer.parseInt(cell.getStringCellValue().trim());
        } catch (NumberFormatException e) {
          return 0;
        }
      default:
        return 0;
    }
  }
}
