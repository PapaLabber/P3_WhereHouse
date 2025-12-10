package group10.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Springboot controller for routing requests from frontend
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class Controller {

  // Declaring variables for future use
  @Value("${app.output-dir:./outputFile}")
  private String outputDirPath;

  @Autowired
  private OutputResult outputResult;

  @Autowired
  private WarehouseAllocator allocator;

  @Autowired
  private DashboardService dashboardService;

  private static final Logger logger = LoggerFactory.getLogger(Controller.class);

  private List<RealizedCapacity> lastCapacities = new ArrayList<>();
  private List<Result> lastResults = new ArrayList<>();
/* 
  @GetMapping("/fillFilters")
  public ResponseEntity<?> fillFilters() {
    List<String> countryOptions = new ArrayList<>();
    List<Integer> yearOptions = 

    return ResponseEntity.ok()
        .headers()
        .body();
  }*/

  /**
   * 
   * @param file          the file inputted by the user containing the information
   *                      needed
   * @param wantedCountry the country the user inputted in the filter
   * @param wantedYear    the year the user inputted in the filter
   * @return HTTP statuses based on what happens during the routing
   * @throws IOException            if the file is not read or does not exist
   * @throws InvalidFormatException if the uploaded file is not a valid or
   *                                readable .xlsx Excel file
   */
  @PostMapping("/export")
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
      @RequestParam("wantedCountry") String wantedCountry,
      @RequestParam("wantedYear") int wantedYear)
      throws IOException, InvalidFormatException {

    // Validate that a file was actually provided in the request
    if (file == null || file.isEmpty()) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body("No file uploaded.");
    }

    // Validate that the file has a valid name and type
    String originalName = file.getOriginalFilename();
    if (originalName == null || !originalName.toLowerCase().endsWith(".xlsx")) {
      return ResponseEntity
          .badRequest()
          .body("Invalid file. Please upload an .xlsx file.");
    }

    // Construct the output filename based on user-selected filters
    String fileName = "AllocatedResult" + wantedYear + wantedCountry + ".xlsx";

    File tempFile = null;

    try {
      // Persist the uploaded file temporarily so Apache POI can read it
      tempFile = File.createTempFile("upload-", ".xlsx");
      file.transferTo(tempFile);

      // Parse and filter the excel input according to user parameters
      ExcelReader reader = new ExcelReader(tempFile);

      List<CapacityRequest> requests = reader.filterRequest(wantedCountry, wantedYear);
      List<RealizedCapacity> capacities = reader.getRealizedCap(wantedCountry, wantedYear);

      // Execute the allocation algorithm based on extracted data
      List<Result> results = allocator.Allocator(requests, capacities);

      // Make results available for the dashboard endpoint (thread-safe update)
      synchronized (this) {
        this.lastCapacities = capacities;
        this.lastResults = results;
      }

      // Write the computed allocation results to a new Excel output file
      Path outputPath = outputResult.writeResultsToExcel(results, fileName);

      File outputFile = outputPath.toFile();
      if (!outputFile.exists()) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Output file could not be created.");
      }

      // Prepare the generated Excel file for download by the frontend
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
      // Memory Cleanup
      if (tempFile != null && tempFile.exists()) {
        try {
          Files.delete(tempFile.toPath());
        } catch (IOException e) {
          logger.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath(), e);
        }
      }
    }
  }

  // Export dashboard data
  @GetMapping("/dashboard")
  public synchronized List<WarehouseDashboard> getDashboard() {
    if (lastCapacities.isEmpty() || lastResults.isEmpty()) {
      return Collections.emptyList();
    }

    return dashboardService.buildDashboard(lastCapacities, lastResults);
  }
}
