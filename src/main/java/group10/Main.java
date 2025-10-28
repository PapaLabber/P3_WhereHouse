package group10;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import group10.Reader.Excelreader;

public class Main {
public static void main(String[] args) throws InvalidFormatException, IOException {
		File excelFile = Paths.get("").resolve("sample_employee_data.xlsx").toFile();
		Excelreader reader = new Excelreader(excelFile);
		reader.readFromExcelFile();
	}
}
