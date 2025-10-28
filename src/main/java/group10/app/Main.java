package group10.app;

import group10.excel.ExcelReader;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class Main {
public static void main(String[] args) throws InvalidFormatException, IOException {
		File excelFile = Paths.get("").resolve("sample_employee_data.xlsx").toFile();
		ExcelReader reader = new ExcelReader(excelFile);
		reader.readFromExcelFile();
	}
}
    

