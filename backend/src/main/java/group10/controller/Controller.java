package group10.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import group10.algorithms.WarehouseAllocator;
import group10.dashboard.DashboardService;
import group10.dashboard.DashboardService.WarehouseDashboard;
import group10.excel.CapacityRequest;
import group10.excel.ExcelReader;
import group10.excel.OutputResult;
import group10.excel.RealizedCapacity;
import group10.excel.Result;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class Controller {

  @Value("${app.output-dir:./outputFile}")
  private String outputDirPath;

  @Autowired
  private OutputResult outputResult;

  @Autowired
  private WarehouseAllocator allocator;

  @Autowired
  private DashboardService dashboardService;

  private List<RealizedCapacity> lastCapacities = new ArrayList<>();
  private List<Result> lastResults = new ArrayList<>();

  @PostMapping("/export")
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam("wantedCountry") String wantedCountry,
                                  @RequestParam("wantedYear") int wantedYear)
      throws IOException, InvalidFormatException {

    // 1) Tjek fil
    if (file == null || file.isEmpty()) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("No file uploaded.");
    }

    String originalName = file.getOriginalFilename();
    if (originalName == null || !originalName.toLowerCase().endsWith(".xlsx")) {
      return ResponseEntity
          .badRequest()
          .body("Invalid file. Please upload an .xlsx file.");
    }

    // 2) Generér outputfilnavn "AllocatedResult<år><land>.xlsx"
    String fileName = "AllocatedResult" + wantedYear + wantedCountry + ".xlsx";

    File tempFile = null;

    try {
      // 3) Gem upload midlertidigt
      tempFile = File.createTempFile("upload-", ".xlsx");
      file.transferTo(tempFile);

      // 4) Læs data fra Excel via ExcelReader
      ExcelReader reader = new ExcelReader(tempFile);

      List<CapacityRequest> requests = reader.filterRequest(wantedCountry, wantedYear);
      List<RealizedCapacity> capacities = reader.getRealizedCap(wantedCountry, wantedYear);

      // 5) Kør OR-Tools algoritmen
      List<Result> results = allocator.Allocator(requests, capacities);

      // opdater for getDashboard
      synchronized (this) {
        this.lastCapacities = capacities;
        this.lastResults = results;
      }

      // 6) Skriv resultater til excel med OutputResult
      Path outputPath = outputResult.writeResultsToExcel(results, fileName);

      File outputFile = outputPath.toFile();
      if (!outputFile.exists()) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Output file could not be created.");
      }

      Resource resource = new FileSystemResource(outputFile);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION,
          "attachment; filename=\"" + outputFile.getName() + "\"");
      headers.add(HttpHeaders.CONTENT_TYPE,
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

      return ResponseEntity.ok()
          .headers(headers)
          .body(resource);

    } finally {
      if (tempFile != null && tempFile.exists()) {
        // tempFile.delete();
      }
    }
  }

  // export dashboard data
  @GetMapping("/dashboard")
  public synchronized List<WarehouseDashboard> getDashboard() {
    if (lastCapacities.isEmpty() || lastResults.isEmpty()) {
      return Collections.emptyList();
    }

    return dashboardService.buildDashboard(lastCapacities, lastResults);
  }
}
