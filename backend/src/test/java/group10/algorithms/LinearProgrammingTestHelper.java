package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

/**
 * Helper class for unit testing LinearProgramming.
 * Provides a method to solve LP instances with custom data and return the result status.
 */
public class LinearProgrammingTestHelper {

    /**
     * Solves a linear programming problem with the given parameters.
     * Returns the solver result status as a string: "OPTIMAL", "INFEASIBLE", "UNBOUNDED", "ABNORMAL", or "NOT_SOLVED".
     * 
     * @param warehouses          number of warehouses
     * @param products            number of products
     * @param factories           number of factories
     * @param transportDistances  cost matrix [warehouses][factories]
     * @param demand              demand matrix [products][factories]
     * @param warehouseCapacities capacity matrix [products][warehouses]
     * @return the result status as a string
     */
    public static String solveLPWithData(
            int warehouses,
            int products,
            int factories,
            double[][] transportDistances,
            double[][] demand,
            double[][] warehouseCapacities) {

        // Initialize OR-Tools and create solver
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            return "SOLVER_UNAVAILABLE";
        }

        double infinity = MPSolver.infinity();
        MPVariable[][][] x = new MPVariable[warehouses][products][factories];

        // Define variables
        for (int w = 0; w < warehouses; w++) {
            for (int p = 0; p < products; p++) {
                for (int f = 0; f < factories; f++) {
                    x[w][p][f] = solver.makeNumVar(0.0, infinity, "x[" + w + "][" + p + "][" + f + "]");
                }
            }
        }

        // Capacity constraints per (warehouse, product)
        for (int p = 0; p < products; p++) {
            for (int w = 0; w < warehouses; w++) {
                double capacity = warehouseCapacities[p][w];
                MPConstraint capacityConstraint = solver.makeConstraint(0.0, capacity, "capacity_p" + p + "_w" + w);
                for (int f = 0; f < factories; f++) {
                    capacityConstraint.setCoefficient(x[w][p][f], 1);
                }
            }
        }

        // Demand constraints per product and factory
        for (int p = 0; p < products; p++) {
            for (int f = 0; f < factories; f++) {
                MPConstraint demandConstraint = solver.makeConstraint(demand[p][f], demand[p][f], "demand_" + p + "_" + f);
                for (int w = 0; w < warehouses; w++) {
                    demandConstraint.setCoefficient(x[w][p][f], 1);
                }
            }
        }

        // Objective: Minimize transport cost
        MPObjective objective = solver.objective();
        for (int w = 0; w < warehouses; w++) {
            for (int p = 0; p < products; p++) {
                for (int f = 0; f < factories; f++) {
                    objective.setCoefficient(x[w][p][f], transportDistances[w][f]);
                }
            }
        }
        objective.setMinimization();

        // Solve and return status
        MPSolver.ResultStatus resultStatus = solver.solve();
        return resultStatus.toString();
    }
}
