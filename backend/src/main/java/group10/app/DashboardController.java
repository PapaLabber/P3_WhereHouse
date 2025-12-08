package group10.app;

import group10.dashboard.DashboardService;
import group10.dashboard.WarehouseDashboard;
import group10.excel.RealizedCapacity;
import group10.excel.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * REST controller exposing the dashboard API.
 * Frontend calls GET /api/dashboard to retrieve the current view.
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    private final AllocationState allocationState;


    public DashboardController(DashboardService dashboardService,
                               AllocationState allocationState) {
        this.dashboardService = dashboardService;
        this.allocationState = allocationState;
    }

    /**
     * Returns the dashboard data for the last allocation run.
     * If no allocation has been run yet, returns an empty list.
     */
    @GetMapping("/api/dashboard")
    public List<WarehouseDashboard> getDashboard() {
        if (!allocationState.hasData()) {
            // No data available yet (no file processed / algorithm not run)
            return Collections.emptyList();
        }

        List<RealizedCapacity> capacities = allocationState.getLastCapacities();
        List<Result> results = allocationState.getLastResults();

        return dashboardService.buildDashboard(capacities, results);
    }
}
