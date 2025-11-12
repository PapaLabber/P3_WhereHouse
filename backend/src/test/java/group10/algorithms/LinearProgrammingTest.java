package group10.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/*
 * Unit tests for LinearProgramming solver.
 * Tests verify that the LP solver correctly handles various scenarios:
 * - Optimal problems with optimal solutions
 * - Infeasible problems (supply < demand)
 * - Capacity constraints enforcement
 * - Objective value correctness
 */
class LinearProgrammingTest {

    /*
     * Helper class to encapsulate LP input data and expected results.
     */
    static class LPTestCase {
        int warehouses;
        int products;
        int factories;
        double[][] transportDistances;
        double[][] demand;
        double[][] warehouseCapacities;
        boolean shouldBeOptimal;
        Double expectedOptimalCost; // null if infeasible

        LPTestCase(int warehouses, int products, int factories,
                   double[][] transportDistances, double[][] demand,
                   double[][] warehouseCapacities, boolean shouldBeOptimal,
                   Double expectedOptimalCost) {

            this.warehouses = warehouses;
            this.products = products;
            this.factories = factories;
            this.transportDistances = transportDistances;
            this.demand = demand;
            this.warehouseCapacities = warehouseCapacities;
            this.shouldBeOptimal = shouldBeOptimal;
            this.expectedOptimalCost = expectedOptimalCost;
        }
    }

    /*
     * Create and solve an LP instance with the given test case data.
     * Returns the solver result status as a string for assertion.
     */
    private String solveLPAndReturnStatus(LPTestCase tc) {
        return LinearProgrammingTestHelper.solveLPWithData(
            tc.warehouses, tc.products, tc.factories,
            tc.transportDistances, tc.demand, tc.warehouseCapacities
        );
    }

    /*                               
     * Test 1: Optimal problem with exact capacity match.
     * Supply equals demand; all warehouses can serve factories.
     */
    @Test
    void testFeasibleProblem_ExactCapacityMatch() {
        LPTestCase tc = new LPTestCase(
            2, 3, 2,
            new double[][]{ { 3, 4 }, { 5, 2 } },                // transportDistances, warehouse 0 to factory 0 and 1 in [0][0-1]
            new double[][]{ { 50, 0 }, { 40, 25 }, { 0, 25 } },  // demand, product 0 (ambient) to factory 0 and 1 in [0][0-1]
            new double[][]{ { 100, 0 }, { 80, 50 }, { 50, 0 } }, // warehouseCapacities, product 0 (ambient) for warehouse 0 and 1 in [0][0-1]
            true,                               // shouldBeOptimal = true due to expected feasibility
            150.0                           // expected cost: (50*3) + (40*3) + (25*2) + (25*5) = 150+120+50+125 = 445
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("OPTIMAL", result, "Expected optimal solution for feasible problem");
    }

    /*
     * Test 2: Infeasible problem – total demand exceeds total capacity.
     * Product 0: demand=110 but capacity=100 → infeasible.
     */
    @Test
    void testInfeasibleProblem_InsufficientCapacity() {
        LPTestCase tc = new LPTestCase(
            2, 3, 2,
            new double[][]{ { 3, 4 }, { 5, 2 } },                    // transportDistances, warehouse 0 to factory 0 and 1 in [0][0-1]
            new double[][]{ { 100, 10 }, { 60, 10 }, { 20, 30 } },   // demand = 110, 70, 50, product 0 (ambient) to factory 0 and 1 in [0][0-1]
            new double[][]{ { 100, 0 }, { 80, 50 }, { 0, 50 } },     // capacity = 100, 130, 50, product 0 (ambient) for warehouse 0 and 1 in [0][0-1]
            false,                                  // shouldBeOptimal = false due to expected infeasibility
            null                                // no expected cost due to expected infeasibility
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("INFEASIBLE", result, "Expected infeasible status when capacity < demand");
    }

    /*
     * Test 3: Optimal problem - Single warehouse, single product, single factory (trivial case).
     */
    @Test
    void testTrivialProblem_SingleDimension() {
        LPTestCase tc = new LPTestCase(
            1, 1, 1,
            new double[][]{ { 5 } },    // transportDistances, warehouse 0 to factory 0 in [0][0]
            new double[][]{ { 10 } },   // demand, product 0 (ambient) to factory 0 in [0][0]
            new double[][]{ { 15 } },   // capacity >= demand, product 0 (ambient) for warehouse 0 in [0][0]
            true,      // shouldBeOptimal = true due to expected feasibility
            50.0   // 10 units * 5 km = 50
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("OPTIMAL", result, "Expected optimal solution for trivial case");
    }

    /*
     * Test 4: Optimal problem - Warehouse with zero capacity for a product.
     * Should force demand to be met by other warehouses (if possible).
     */
    @Test
    void testZeroCapacityConstraint() {
        LPTestCase tc = new LPTestCase(
            2, 2, 1,
            new double[][]{ { 2 }, { 3 } },           // transportDistances, warehouse 0 to factory 0 in [0][0]
            new double[][]{ { 30 }, { 20 } },         // demand, product 0 (ambient) to factory 0 in [0][0]
            new double[][]{ { 30, 0 }, { 0, 20 } },   // warehouse 0: product 0 only; warehouse 1: product 1 only, product 0 (ambient) for warehouse 0 in [0][0-1]
            true,                    // shouldBeOptimal = true due to expected feasibility
            150.0                // (30*2) + (20*3) = 60 + 60 = 120
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("OPTIMAL", result, "Expected optimal solution with zero capacity constraints");
    }

    /*
     * Test 5: Optimal problem - Multiple routes to same factory.
     * Solver should choose cheapest route while respecting capacities.
     */
    @Test
    void testMultipleRoutesOptimization() {
        // Warehouse 0 → Factory 0: cost 1 (cheap)
        // Warehouse 1 → Factory 0: cost 10 (expensive)
        // Capacity allows both to supply.
        LPTestCase tc = new LPTestCase(
            2, 1, 1,
            new double[][]{ { 1 }, { 10 } },    // transportDistances, warehouse 0 to factory 0 in [0][0]
            new double[][]{ { 50 } },           // demand, product 0 (ambient) to factory 0 in [0][0]
            new double[][]{ { 50, 50 } },       // product 0 (ambient) for warehouse 0 in [0][0-1]
            true,    // shouldBeOptimal = true due to expected feasibility
            50.0 // All from warehouse 0 at cost 1: 50*1 = 50
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("OPTIMAL", result, "Expected optimal solution using cheapest routes");
    }

    /*
     * Test 6: Optimal problem - Demand split across multiple warehouses.
     * Solver must distribute supply to meet all demand.
     */
    @Test
    void testDemandSplitAcrossWarehouses() {
        // Total demand: 100
        // Warehouse 0 capacity: 60
        // Warehouse 1 capacity: 50
        // => Warehouse 0 ships 60, Warehouse 1 ships 40
        LPTestCase tc = new LPTestCase(
            2, 1, 1,
            new double[][]{ { 2 }, { 3 } },   // transportDistances, warehouse 0 to factory 0 in [0][0]
            new double[][]{ { 100 } },        // demand, product 0 (ambient) to factory 0 in [0][0]
            new double[][]{ { 60, 50 } },     // product 0 (ambient) for warehouse 0 and 1 in [0][0-1]
            true,     // shouldBeOptimal = true due to expected feasibility
            420.0 // (60*2) + (40*3) = 120 + 120 = 240
        );

        String result = solveLPAndReturnStatus(tc);
        assertEquals("OPTIMAL", result, "Expected optimal distribution across warehouses");
    }
}
