package group10;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import group10.dashboard.DashboardService;
import group10.dashboard.DashboardService.WarehouseDashboard;
import group10.excel.CapacityRequest;
import group10.excel.ProductionSite;
import group10.excel.RealizedCapacity;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;

class DashboardServiceTest {

    @Test
    void buildDashboard_aggregatesAndSortsCorrectly() {
        DashboardService service = new DashboardService();

        // Capacity requests (exact values don't matter for dashboard, but must exist)
        List<CapacityRequest> requests = new ArrayList<>();
        requests.add(new CapacityRequest(100, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"),   1, 2026));
        requests.add(new CapacityRequest(100, Temperature.COLD,   ProductionSite.fromName("Kalundborg"), 2, 2026));
        requests.add(new CapacityRequest(100, Temperature.FREEZE, ProductionSite.fromName("Hjørring"),    3, 2026));

        // ---- Realized capacities (TOTAL capacity per warehouse) ----
        // PS PAC I total = 100 (50 ambient + 50 cold)
        // PS PAC II total = 50 (50 freeze)
        List<RealizedCapacity> capacities = new ArrayList<>();
        capacities.add(new RealizedCapacity(50, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        capacities.add(new RealizedCapacity(50, Temperature.COLD,   Warehouse.fromName("PS PAC I"), 2026));
        capacities.add(new RealizedCapacity(50, Temperature.FREEZE, Warehouse.fromName("PS PAC II"), 2026));

        // ---- Results (USED capacity per warehouse + temperature breakdown) ----
        // PS PAC I: ambient = 30, cold = 20 → used = 50
        // PS PAC II: freeze = 10          → used = 10
        List<Result> results = new ArrayList<>();
        results.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 30, requests.get(0)));
        results.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.COLD,   20, requests.get(1)));
        results.add(new Result(Warehouse.fromName("PS PAC II"), Temperature.FREEZE, 10, requests.get(2)));

        // Act
        List<DashboardService.WarehouseDashboard> dashboards = service.buildDashboard(capacities, results);

        // Expect 2 warehouses
        assertEquals(2, dashboards.size());

        DashboardService.WarehouseDashboard first = dashboards.get(0);
        DashboardService.WarehouseDashboard second = dashboards.get(1);

        // First: PS PAC I (higher utilisation: 50 / 100 = 50 %)
        assertEquals("PS PAC I", first.getWarehouseName());
        assertEquals(100, first.getTotalCapacity());
        assertEquals(50, first.getUsedCapacity());
        assertEquals(50, first.getRemainingCapacity());
        assertEquals(30, first.getAmbient());
        assertEquals(20, first.getCold());
        assertEquals(0, first.getFreeze());
        assertEquals(50.0, first.getUtilisationPercent(), 0.0001);

        // Second: PS PAC II (10 / 50 = 20 %)
        assertEquals("PS PAC II", second.getWarehouseName());
        assertEquals(50, second.getTotalCapacity());
        assertEquals(10, second.getUsedCapacity());
        assertEquals(40, second.getRemainingCapacity());
        assertEquals(0, second.getAmbient());
        assertEquals(0, second.getCold());
        assertEquals(10, second.getFreeze());
        assertEquals(20.0, second.getUtilisationPercent(), 0.0001);
    }

    @Test
    void buildDashboard_failure() {
        DashboardService service = new DashboardService();

        // empty capacities, no warehouses exist, no dashboard entries
        List<RealizedCapacity> capacities = new ArrayList<>();
        List<Result> results = new ArrayList<>();

        // Act
        List<WarehouseDashboard> dashboards = service.buildDashboard(capacities, results);

        // Assert
        assertNotNull(dashboards, "Dashboard list should not be null");
        assertEquals(0, dashboards.size(), "No capacities means no dashboard entries");
    }
}