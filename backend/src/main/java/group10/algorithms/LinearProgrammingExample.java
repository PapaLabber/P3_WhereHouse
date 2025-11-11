package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

public class LinearProgrammingExample {

    public static void LP() {
      Loader.loadNativeLibraries();
      MPSolver solver = MPSolver.createSolver("GLOP");
      if (solver == null) {
        System.err.println("Solver not available.");
        return;
      }

      // Problem dimensions 3D
      int warehouses = 2;
      int products = 2;
      int factories = 2;

      double[][] transportDistances = {
        {3, 4}, // warehouse 0 → factory 0 og 1
        {5, 2}  // warehouse 1 → factory 0 og 1
      };

      double[][] demand = {
        {30, 20}, // product 0 to factory 0 and 1
        {10, 40}  // product 1 to factory 0 and 1
      };

      double[] warehouseCapacities = {100, 80};

      boolean[][] compatibility = {
        {true, false},
        {true, true}
      };

      double infinity = MPSolver.infinity();
      MPVariable[][][] x = new MPVariable[warehouses][products][factories];

      // Define variables
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            // Decision variables ≥ 0.0
            x[w][p][f] = solver.makeNumVar(0.0, infinity, "x[" + w + "][" + p + "][" + f + "]");
          }
        }
      }

      // Capacity constraints per warehouse
      for (int w = 0; w < warehouses; w++) {
        MPConstraint capacityConstraint = solver.makeConstraint(0, warehouseCapacities[w], "capacity_" + w);
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            /*
            * setCoefficient sets a number to be multiplied upon our x, this
            * number is redundant due to our x being dependent on w, p and f.
            */
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

      // Compatibility constraints
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          if (!compatibility[w][p]) {
            for (int f = 0; f < factories; f++) {
              /*
               * After finding a warehouse and product which are not compatible
               * we need to ensure our x is set to 0, but our x is defined by w, p and f
               * so we loop through our f to set all x's of the specific
               * w and p to 0.
               */
              MPConstraint compConstraint = solver.makeConstraint(0.0, 0.0);
              compConstraint.setCoefficient(x[w][p][f], 1);
            }
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

    // Solve
    final MPSolver.ResultStatus resultStatus = solver.solve();


    /*
     *     if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Optimal transport cost = " + objective.value());
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            double val = x[w][p][f].solutionValue();
            if (val > 0.0) {
              System.out.printf("Send %.0f stk af produkt %d fra lager %d til fabrik %d\n", val, p, w, f);
            }
            //if (val > 0.0) {System.out.printf("x[%d][%d][%d] = %.2f\n", w, p, f, val);}
          }
        }
      }
    } else {
      System.err.println("No optimal solution found.");
    }
  }
     */
    // Output solution
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Optimal transportafstand (km * antal): " + objective.value());
      double totalDistance = 0.0;
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            double quantity = x[w][p][f].solutionValue();
            if (quantity > 0.0) {
              double distance = transportDistances[w][f];
              double cost = distance * quantity;
              totalDistance += cost;
              System.out.printf("Produkt %d: Lager %d → Fabrik %d | Antal: %.0f | Afstand: %.2f km | Total: %.2f km\n",
                  p, w, f, quantity, distance, cost);
            }
          }
        }
      }
      System.out.printf("Samlet transportafstand: %.2f km\n", totalDistance);
    } else {
      System.err.println("Ingen optimal løsning fundet.");
    }
  }
}
