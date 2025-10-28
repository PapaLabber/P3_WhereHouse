package group10;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excelreader {

	private Workbook workbook;
	private Sheet sheet;

	public Excelreader(File excelFile) throws InvalidFormatException, IOException {
		workbook = new XSSFWorkbook(excelFile);
		sheet = workbook.getSheetAt(0);
	}

	public void readFromExcelFile() throws IOException {
		// First pass: calculate maximum width for each column
		int[] maxWidths = calculateColumnWidths();
		
		// Second pass: print with proper alignment
		for (Row row : sheet) {
			if (row == null) continue;
			
			// Iterate through ALL columns, not just non-blank ones
			int lastColumn = row.getLastCellNum();
			for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
				Cell cell = row.getCell(cellNum);
				String value = getCellValueAsString(cell);
				int width = maxWidths[cellNum];
				System.out.printf("%-" + width + "s  ", value);
			}
			System.out.println();
		}
		
		workbook.close();
	}
	
	private int[] calculateColumnWidths() {
		int maxColumns = 0;
		
		// Find max number of columns
		for (Row row : sheet) {
			if (row != null && row.getLastCellNum() > maxColumns) {
				maxColumns = row.getLastCellNum();
			}
		}
		
		int[] maxWidths = new int[maxColumns];
		
		// Calculate max width for each column
		for (Row row : sheet) {
			if (row == null) continue;
			
			int lastColumn = row.getLastCellNum();
			for (int cellNum = 0; cellNum < lastColumn; cellNum++) {
				Cell cell = row.getCell(cellNum);
				String value = getCellValueAsString(cell);
				if (value.length() > maxWidths[cellNum]) {
					maxWidths[cellNum] = value.length();
				}
			}
		}
		
		return maxWidths;
	}
	
	private String getCellValueAsString(Cell cell) {
		// Handle null cells (blank cells)
		if (cell == null) {
			return "";
		}
		
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC: 
				if (DateUtil.isCellDateFormatted(cell)) {
					return new SimpleDateFormat("MM-dd-yyyy").format(cell.getDateCellValue());
				} else {
					return String.valueOf((int) cell.getNumericCellValue());
				}
			case BLANK:
				return "";
			default:
				return "";
		}
	}
}