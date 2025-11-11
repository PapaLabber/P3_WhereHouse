package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

public class LinearProgrammingExample {

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
      int products = 2;   // products = P notation
      int factories = 2;  // factories = F notation

      double[][] transportDistances = { // transportDistances = T w,f notation
        {3, 4}, // warehouse 0 to factory 0 and 1
        {5, 2}  // warehouse 1 to factory 0 and 1
      };

      double[][] demand = { // demand = D p,f notation
        {100, 10}, // product ambient 0 to factory 0 and 1
        {60, 10},  // product cold 1 to factory 0 and 1
        {20, 30}   // product freeze 2 to factory 0 and 1
      };

      double[][] warehouseCapacities = { // warehouseCapacities = C w notation
        {100, 80},  // Ambient for warehouse 0 and 1
        {80, 50},   // Cold for warehouse 0 and 1
        {10, 50}    // Freeze for warehouse 0 and 1
      }; 

      boolean[][] compatibility = { // compatibility = A w,p notation
        {true, false},
        {true, true}
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
          /*
          * setCoefficent sets a number to be multiplied upon our x, this
          * number is the transport distance from warehouse w to factory f
          * as an example: transportDistances[w][f] * x[w][p][f]
          */
          objective.setCoefficient(x[w][p][f], transportDistances[w][f]);
        }
      }
    }
    // Finds the minimization for the objective with the coefficients from the previous nested for-loops
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
              totalCost += cost;
              System.out.printf("Product %d: Warehouse %d to Factory %d | Allocated amount: %.2f | Distance: %.2f km | Total cost: %.2f\n",
                  p, w, f, currentX, transportDistances[w][f], totalCost);
            }
          }
        }
      }
      System.out.printf("Combined cost: %.2f\n", totalCost); // think about
    } else {
      System.err.println("No optimal solution found. " + resultStatus);
    }
    /* Previous iteration for the above output solution

    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
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
    */
  }

  
}
