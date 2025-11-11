package group10.controller;

import group10.excel.*;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.catalina.connector.Response;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class Controller {
    @Value("${app.output-dir}")
    private String outputDirPath;

    @Autowired
    private OutputResult outputResult;

    @PostMapping("/fileUpload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("wantedCountry") String wantedCountry, @RequestParam("wantedYear") int wantedYear)
            throws IOException, InvalidFormatException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid file. Please upload an .xlsx file.");
        }

        try {
            File tempFile = File.createTempFile("upload-", ".xlsx");
            file.transferTo(tempFile);

            ExcelReader reader = new ExcelReader(tempFile);

            List<CapacityRequest> wantedRequests = reader.filterRequest(wantedCountry, wantedYear);
            for (CapacityRequest req : wantedRequests) {
                System.out.println(req);
            }

            List<RealisedCapacity> capacities = reader.warehouseCapacity(wantedCountry, wantedYear);
            for (RealisedCapacity cap : capacities) {
                System.out.println(cap);
            }

            List<Result> results = new ArrayList<>();

            // Kør algoritme

            String fileName = "AllocatedResult" + wantedYear + wantedCountry + ".xlsx";

            outputResult.writeResultsToExcel(results, fileName);

            tempFile.delete();

        } catch (IOException e) {
            return ResponseEntity
                    .status(500)
                    .body("Error while processing file.");
        }

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<?> download(@RequestParam String fileName) {
        File file = new File(outputDirPath, fileName);

        if (!file.exists()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentLength(file.length())
                .body(resource);
    }
}