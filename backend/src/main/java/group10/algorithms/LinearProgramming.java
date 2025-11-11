package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

public class LinearProgramming {

  public static void LP() {
    // Initialzing OR-Tools and creating the solver.
    Loader.loadNativeLibraries();
    MPSolver solver = MPSolver.createSolver("GLOP");
    if (solver == null) {
      System.err.println("Solver not available.");
      return;
    }

    // Notations from report
    int warehouses = 2; // warehouses = W notation
    int products = 3; // products = P notation
    int factories = 2; // factories = F notation

    double[][] transportDistances = { // transportDistances = T_{w,f} notation
        { 3, 4 }, // warehouse 0 to factory 0 and 1
        { 5, 2 } // warehouse 1 to factory 0 and 1
    };

    double[][] demand = { // demand = D_{p,f} notation
        { 50, 0 }, // product 0 (ambient) to factory 0 and 1
        { 40, 25 }, // product 1 (cold) to factory 0 and 1
        { 0, 25 } // product 2 (freeze) to factory 0 and 1
    };

    double[][] warehouseCapacities = { // warehouseCapacities = C_{p,w} notation
        { 100, 0 }, // product 0 (ambient) for warehouse 0 and 1
        { 80, 50 }, // product 1 (cold) for warehouse 0 and 1
        { 50, 0 }   // product 2 (freeze) for warehouse 0 and 1
    };

    // Initializing baseline infinity
    double infinity = MPSolver.infinity();
    // Initializing decision variable x
    MPVariable[][][] x = new MPVariable[warehouses][products][factories];

    // Define variables
    for (int w = 0; w < warehouses; w++) {
      for (int p = 0; p < products; p++) {
        for (int f = 0; f < factories; f++) {
          // Decision variables >= 0.0
          x[w][p][f] = solver.makeNumVar(0.0, infinity, "x[" + w + "][" + p + "][" + f + "]");
        }
      }
    }

    // Capacity constraints per (warehouse, product)
    // warehouseCapacities is double[][] with dimensions [products][warehouses]
    for (int p = 0; p < products; p++) {
      for (int w = 0; w < warehouses; w++) {
        double capacity = warehouseCapacities[p][w];
        // sum_f x[w][p][f] <= capacity
        MPConstraint capacityConstraint = solver.makeConstraint(0.0, capacity, "capacity_p" + p + "_w" + w);
        for (int f = 0; f < factories; f++) {
          capacityConstraint.setCoefficient(x[w][p][f], 1);
        }
      }
    }

    // Demand constraints per product and factory
    for (int p = 0; p < products; p++) {
      for (int f = 0; f < factories; f++) {
        // Tight constraint: supply = demand.
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
          /*
           * setCoefficent sets a number to be multiplied upon our x, this
           * number is the transport distance from warehouse w to factory f
           * as an example: transportDistances[w][f] * x[w][p][f]
           */
          objective.setCoefficient(x[w][p][f], transportDistances[w][f]);
        }
      }
    }
    // Finds the minimization for the objective with the coefficients from the
    // previous nested for-loops
    objective.setMinimization();

    // Solve
    final MPSolver.ResultStatus resultStatus = solver.solve();

    // Output solution
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Optimal cost (Distance * Allocated amount): " + objective.value());
      double totalCost = 0.0;
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            double currentX = x[w][p][f].solutionValue();
            if (currentX > 0) {
              double cost = transportDistances[w][f] * currentX;
              totalCost += cost; // Delete dat shiiiit
              System.out.printf(
                  "Product %d: Warehouse %d to Factory %d | Allocated amount: %.2f | Distance: %.2f km | Cost: %.2f\n",
                  p, w, f, currentX, transportDistances[w][f], cost);
            }
          }
        }
      }
      System.out.printf("Combined cost: %.2f\n", totalCost); // think about
    } else {
      System.err.println("No optimal solution found. " + resultStatus);
    }
    /*
     * Previous iteration for the above output solution
     * 
     * if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
     * System.out.println("Optimal transport cost = " + objective.value());
     * for (int w = 0; w < warehouses; w++) {
     * for (int p = 0; p < products; p++) {
     * for (int f = 0; f < factories; f++) {
     * double val = x[w][p][f].solutionValue();
     * if (val > 0.0) {
     * System.out.printf("Send %.0f stk af produkt %d fra lager %d til fabrik %d\n",
     * val, p, w, f);
     * }
     * //if (val > 0.0) {System.out.printf("x[%d][%d][%d] = %.2f\n", w, p, f, val);}
     * }
     * }
     * }
     * } else {
     * System.err.println("No optimal solution found.");
     * }
     */
  }

}
