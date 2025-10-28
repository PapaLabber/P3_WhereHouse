package group10.Reader;

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
		for (Row row : sheet) {
			System.out.println();

			for (Cell cell : row) {
				printCellValue(cell);
				System.out.print("\t");
			}
		}

		workbook.close();
	}


	private void printCellValue(Cell cell) {
		 switch (cell.getCellType()) {
         case STRING:
         	System.out.print(cell.getStringCellValue());
         	break;
         case NUMERIC:
         	if (DateUtil.isCellDateFormatted(cell)) {
         	 	System.out.print(new SimpleDateFormat("MM-dd-yyyy").format(cell.getDateCellValue()));
         	} else {
         	 	System.out.print((int) cell.getNumericCellValue());
         	}

         	break;
         default:
         	break;
     }
	}

/*
 *   public void readFromExcelFile() throws IOException {
    // Finder kolonneindeks for "Storage Condition"
    int storageConditionIndex = -1;
    Row header = sheet.getRow(0); // Første række (overskrifter)

    for (Cell cell : header) {
      if (cell.getStringCellValue().equalsIgnoreCase("Storage Condition")) {
        storageConditionIndex = cell.getColumnIndex(); // Gem kolonneindeks
        break;
      }
    }

    // Stop hvis kolonnen ikke blev fundet
    if (storageConditionIndex == -1) {
      System.out.println("Storage Condition kolonne ikke fundet");
      return;
    }

    // Gennemgå alle rækker i arket (starter fra række 1, fordi række 0 er header)
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) continue; // Spring tomme rækker over

      // Hent cellen i "Storage Condition"-kolonnen
      Cell conditionCell = row.getCell(storageConditionIndex);
      if (conditionCell == null) continue; // Spring hvis cellen er tom

      // Hent tekstværdi og fjern mellemrum i start/slut
      String condition = conditionCell.getStringCellValue().trim();

      // Send til handler, som klassificerer typen
      group10.Handler.ConditionHandler.handle(condition);
    }

    // Lukker Excel-filen
    workbook.close();
  }
 */

}
