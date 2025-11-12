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
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Controller {
    @Value("${app.output-dir}")
    private String outputDirPath;

    @Autowired
    private OutputResult outputResult;

    @PostMapping("/export")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
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

        String fileName = "AllocatedResult" + wantedYear + wantedCountry + ".xlsx";
        File tempFile = null;
        Path pathOfFile;

        try {
            tempFile = File.createTempFile("upload-", ".xlsx");
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

            pathOfFile = outputResult.writeResultsToExcel(results, fileName);

        } catch (IOException e) {
            return ResponseEntity
                    .status(500)
                    .body("Error while processing file.");
        } finally {
            if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        }

        Resource resource = new FileSystemResource(pathOfFile);
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Output file not found after processing.");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentLength(resource.contentLength())
                .body(resource);
    }

}