package group10.app;

import group10.dashboard.DashboardService;
import group10.dashboard.WarehouseDashboard;
import group10.excel.RealizedCapacity;
import group10.excel.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * REST controller exposing the dashboard API.
 * Frontend calls GET /api/dashboard to retrieve the current view.
 */
@RestController
public class DashboardController {

    private final DashboardService dashboardService;
    private final AllocationState allocationState;

    public DashboardController(DashboardService dashboardService,
                               AllocationState allocationState) {
        this.dashboardService = dashboardService;
        this.allocationState = allocationState;
    }

    @GetMapping("/api/dashboard")
    public List<WarehouseDashboard> getDashboard() {
        if (!allocationState.hasData()) {
            // Either return empty list or throw 404/400, depending on your preference
            return Collections.emptyList();
        }

        List<RealizedCapacity> capacities = allocationState.getLastCapacities();
        List<Result> results = allocationState.getLastResults();

        return dashboardService.buildDashboard(capacities, results);
    }
}
