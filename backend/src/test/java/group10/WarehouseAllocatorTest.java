package group10;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import group10.algorithms.WarehouseAllocator;
import group10.excel.CapacityRequest;
import group10.excel.ProductionSite;
import group10.excel.RealizedCapacity;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;

/*
 * Unit tests for LinearProgramming solver.
 * Tests verify that the LP solver correctly handles various scenarios:
 * - Optimal problems with optimal solutions
 * - Infeasible problems (supply < demand)
 * - Capacity constraints enforcement
 * - Objective value correctness
 */
class WarehouseAllocatorTest {
    /*                               
     * Test 1: Optimal problem with exact capacity match.
     * Supply equals demand; all warehouses can serve factories.
     */
    @Test
    void testFeasibleProblem_ExactCapacityMatch() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(80, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));
        requests.add(new CapacityRequest(80, Temperature.COLD, ProductionSite.fromName("Kalundborg"), 2, 2026));
        requests.add(new CapacityRequest(80, Temperature.FREEZE, ProductionSite.fromName("Hjørring"), 3, 2026));

        realisedCap.add(new RealizedCapacity(125, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realisedCap.add(new RealizedCapacity(125, Temperature.COLD, Warehouse.fromName("PS PAC II"), 2026));
        realisedCap.add(new RealizedCapacity(125, Temperature.FREEZE, Warehouse.fromName("NEFF"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();
        List<Result> result = allocator.Allocator(requests, realisedCap);
        
        List<Result> expected = new ArrayList<>();
        expected.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0)));
        expected.add(new Result(Warehouse.fromName("PS PAC II"), Temperature.COLD, 80, requests.get(1)));
        expected.add(new Result(Warehouse.fromName("NEFF"), Temperature.FREEZE, 80, requests.get(2)));

        assertEquals(expected.toString(), result.toString(), "Expected optimal solution for optimal problem");
    }

    /*
     * Test 2: Infeasible problem – total demand exceeds total capacity.
     * Product 0: demand=110 but capacity=100 → infeasible.
     */
    @Test
    void testInfeasibleProblem_InsufficientCapacity() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(110, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));
        requests.add(new CapacityRequest(100, Temperature.COLD, ProductionSite.fromName("Kalundborg"), 2, 2026));
        requests.add(new CapacityRequest(100, Temperature.FREEZE, ProductionSite.fromName("Hjørring"), 3, 2026));

        realisedCap.add(new RealizedCapacity(109, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realisedCap.add(new RealizedCapacity(99, Temperature.COLD, Warehouse.fromName("PS PAC II"), 2026));
        realisedCap.add(new RealizedCapacity(99, Temperature.FREEZE, Warehouse.fromName("NEFF"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();

        List<Result> result = allocator.Allocator(requests, realisedCap);
        List<Result> expected = new ArrayList<>(); // No data mocked since no result is expected

        assertEquals(expected.toString(), result.toString(), "Expected no allocation for infeasible problem");
    }

    /*
     * Test 3: Optimal problem - Single warehouse, single product, single factory (trivial case).
     */
    @Test
    void testTrivialProblem_SingleDimension() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(80, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));

        realisedCap.add(new RealizedCapacity(125, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();

        List<Result> result = allocator.Allocator(requests, realisedCap);
        List<Result> expected = new ArrayList<>();

        expected.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0)));
        
        assertEquals(expected.toString(), result.toString(), "Expected optimal solution for trivial problem");
    }

    /*
     * Test 4: Optimal problem - Warehouse with zero capacity for a product.
     * Should force demand to be met by other warehouses (if possible).
     */
    @Test
    void testZeroCapacityConstraint() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(80, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));
        requests.add(new CapacityRequest(80, Temperature.COLD, ProductionSite.fromName("Kalundborg"), 2, 2026));
        requests.add(new CapacityRequest(80, Temperature.FREEZE, ProductionSite.fromName("Hjørring"), 3, 2026));

        realisedCap.add(new RealizedCapacity(125, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realisedCap.add(new RealizedCapacity(125, Temperature.COLD, Warehouse.fromName("PS PAC II"), 2026));
        realisedCap.add(new RealizedCapacity(125, Temperature.FREEZE, Warehouse.fromName("NEFF"), 2026));
        
        realisedCap.add(new RealizedCapacity(0, Temperature.AMBIENT, Warehouse.fromName("PS HUB"), 2026));
        realisedCap.add(new RealizedCapacity(0, Temperature.COLD, Warehouse.fromName("PS HUB"), 2026));
        realisedCap.add(new RealizedCapacity(0, Temperature.FREEZE, Warehouse.fromName("PS HUB"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();

        List<Result> result = allocator.Allocator(requests, realisedCap);
        List<Result> expected = new ArrayList<>();

        expected.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0)));
        expected.add(new Result(Warehouse.fromName("PS PAC II"), Temperature.COLD, 80, requests.get(1)));
        expected.add(new Result(Warehouse.fromName("NEFF"), Temperature.FREEZE, 80, requests.get(2)));

        assertEquals(expected.toString(), result.toString(), "Expected optimal solution with zero capacity constraints");
    }

    /*
     * Test 5: Optimal problem - Multiple routes to same factory.
     * Solver should choose cheapest route while respecting capacities.
     */
    @Test
    void testMultipleRoutesOptimization() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(80, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));

        realisedCap.add(new RealizedCapacity(125, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realisedCap.add(new RealizedCapacity(125, Temperature.AMBIENT, Warehouse.fromName("PS PAC II"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();

        List<Result> result = allocator.Allocator(requests, realisedCap);
        List<Result> expected = new ArrayList<>();

        expected.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0)));

        assertEquals(expected.toString(), result.toString(), "Expected optimal solution using cheapest routes");
    }

    /*
     * Test 6: Optimal problem - Demand split across multiple warehouses.
     * Solver must distribute supply to meet all demand.
     */
    @Test
    void testDemandSplitAcrossWarehouses() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realisedCap = new ArrayList<>();

        requests.add(new CapacityRequest(100, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));

        realisedCap.add(new RealizedCapacity(80, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realisedCap.add(new RealizedCapacity(20, Temperature.AMBIENT, Warehouse.fromName("PS PAC II"), 2026));

        WarehouseAllocator allocator = new WarehouseAllocator();

        List<Result> result = allocator.Allocator(requests, realisedCap);
        List<Result> expected = new ArrayList<>();

        expected.add(new Result(Warehouse.fromName("PS PAC I"), Temperature.AMBIENT, 80, requests.get(0)));
        expected.add(new Result(Warehouse.fromName("PS PAC II"), Temperature.AMBIENT, 20, requests.get(0)));

        assertEquals(expected.toString(), result.toString(), "Expected optimal distribution across warehouses");
    }

    /*
     * Test 7: computeDistances.
     * 
     */
@Test
    void computeTransportDistances_matchesExpected() {
        List<CapacityRequest> requests = new ArrayList<>();
        List<RealizedCapacity> realised = new ArrayList<>();

        requests.add(new CapacityRequest(10, Temperature.AMBIENT, ProductionSite.fromName("Hillerød"), 1, 2026));
        requests.add(new CapacityRequest(5, Temperature.COLD, ProductionSite.fromName("Kalundborg"), 2, 2026));

        realised.add(new RealizedCapacity(100, Temperature.AMBIENT, Warehouse.fromName("PS PAC I"), 2026));
        realised.add(new RealizedCapacity(50, Temperature.COLD, Warehouse.fromName("NEFF"), 2026));

        double[][] matrix = WarehouseAllocator.computeTransportDistances(requests, realised);

        // values
        for (int c = 0; c < realised.size(); c++) {
            for (int r = 0; r < requests.size(); r++) {
                double expected = Math.hypot(
                    realised.get(c).getWarehouse().getLongitude() - requests.get(r).getProductionSite().getLongitude(),
                    realised.get(c).getWarehouse().getLatitude() - requests.get(r).getProductionSite().getLatitude()
                );
                assertEquals(expected, matrix[c][r], "Expected same distances found in method as above computation");
            }
        }
    }
}
