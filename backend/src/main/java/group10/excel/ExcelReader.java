package group10.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Reader til Excel-inputfilen.
 *
 * Forudsætter et ark med kolonner som mindst:
 *  - "Country"
 *  - "PalletAmount"
 *  - "Year"
 *  - "ProductionSite"
 *  - "Temperature"
 *  - "ID"           (til requests)
 *  - "Warehouse"
 *  - "Capacity"
 *
 * Justér kolonnenavne her, hvis de hedder noget andet i din fil.
 */
public class ExcelReader {

    private final Workbook workbook;
    private final Sheet sheet;

    public ExcelReader(File excelFile) throws InvalidFormatException, IOException {
        this.workbook = new XSSFWorkbook(excelFile); // åbner .xlsx
        this.sheet = workbook.getSheetAt(0);         // bruger første sheet
    }

    /**
     * Filtrér alle CapacityRequest efter land og år.
     * Bruger kolonner:
     *  - "Country"
     *  - "PalletAmount"
     *  - "Year"
     *  - "ProductionSite"
     *  - "Temperature"
     *  - "ID"
     */
    public List<CapacityRequest> filterRequest(String wantedCountry, int wantedYear) throws IOException {
        Iterator<Row> it = sheet.iterator();
        if (!it.hasNext()) { // ingen rækker
            workbook.close();
            return Collections.emptyList();
        }

        // 1. Læs header-række og lav map: header -> kolonneindeks
        Row headerRow = it.next();
        Map<String, Integer> colIndex = getHeaderIndexMap(headerRow);

        List<CapacityRequest> result = new ArrayList<>();

        // 2. Gennemgå alle datarækker
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

            // ID (enten fra kolonnen "ID" eller fallback til rækkenummer)
            int id;
            if (colIndex.containsKey("ID")) {
                id = getIntCell(row, colIndex.get("ID"));
            } else {
                id = row.getRowNum();
            }

            // KORREKT konstruktor‑kald:
            CapacityRequest req = new CapacityRequest(
                    pallets,    // int palletAmount
                    zone,       // Temperature temperature
                    site,       // ProductionSite productionSite
                    id,         // int ID
                    year        // int year
            );

            result.add(req);
        }

        workbook.close();
        return result;
    }

    /**
     * Læs alle RealizedCapacity fra samme sheet (uden filtrering).
     * Bruger kolonner:
     *  - "Warehouse"
     *  - "Capacity"
     *  - "Temperature"
     *  - "Year"
     */
    public List<RealizedCapacity> getRealizedCap() throws IOException {
        List<RealizedCapacity> capacities = new ArrayList<>();

        Iterator<Row> it = sheet.iterator();
        if (!it.hasNext()) { // ingen rækker
            workbook.close();
            return capacities;
        }

        Row headerRow = it.next();
        Map<String, Integer> colIndex = getHeaderIndexMap(headerRow);

        Integer colWarehouse = colIndex.get("Warehouse");
        Integer colCap       = colIndex.get("Capacity");
        Integer colTemp      = colIndex.get("Temperature");
        Integer colYear      = colIndex.get("Year");

        while (it.hasNext()) {
            Row row = it.next();
            if (row == null) continue;

            String whName  = getStringCell(row, colWarehouse);
            int capacity   = getIntCell(row, colCap);
            String tempRaw = getStringCell(row, colTemp);
            int year       = (colYear != null) ? getIntCell(row, colYear) : 0;

            if (whName == null || tempRaw == null || capacity <= 0) continue;

            Temperature zone = Temperature.fromString(tempRaw);
            if (zone == null) {
                System.err.println("Skipping row (warehouse): invalid Temperature '" + tempRaw + "'");
                continue;
            }

            // Byg Warehouse – tilpas hvis du har en anden måde
            Warehouse wh = Warehouse.fromName(whName);

            // korrekt konstruktor-kald:
            RealizedCapacity rc = new RealizedCapacity(
                    capacity,  // int palletAmount
                    zone,      // Temperature temperature
                    wh,        // Warehouse warehouse
                    year       // int year (0 hvis ingen kolonne)
            );

            capacities.add(rc);
        }

        workbook.close();
        return capacities;
    }

    /**
     * Bruges af tests (ExcelReaderTest).
     * Returnerer en liste af RealizedCapacity filtreret på land og år.
     *
     * Hvis dine tests forventer noget andet filter (kun år, kun land osv.),
     * kan du justere filtreringen her.
     */
    public List<RealizedCapacity> warehouseCapacity(String wantedCountry, int wantedYear) throws IOException {
        // Udgangspunkt: brug alle RealizedCapacity og filtrér
        List<RealizedCapacity> all = getRealizedCap();
        List<RealizedCapacity> filtered = new ArrayList<>();

        for (RealizedCapacity rc : all) {
            // Filtrér på år
            if (rc.getYear() != wantedYear) {
                continue;
            }

            // Hvis Warehouse har et land, kan du filtrere her.
            // Eksempel (tilpas til din Warehouse-klasse):
            Warehouse wh = rc.getWarehouse();
            // Hvis Warehouse ikke har country, så fjern dette filter.
            // if (!wh.getCountry().equalsIgnoreCase(wantedCountry)) {
            //     continue;
            // }

            filtered.add(rc);
        }

        return filtered;
    }

    /**
     * Build map of header name -> column index.
     */
    private Map<String, Integer> getHeaderIndexMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            if (cell == null) continue;
            if (cell.getCellType() != CellType.STRING) continue;
            String headerName = cell.getStringCellValue().trim();
            map.put(headerName, cell.getColumnIndex());
        }
        return map;
    }

    /**
     * Læs en celle som String, også hvis den er numerisk/boolsk.
     * Returnerer null hvis tom.
     */
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
     * Læs heltalsværdi fra en celle. Returnerer 0 hvis blank/ikke-numerisk.
     */
    private int getIntCell(Row row, Integer colIdx) {
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
