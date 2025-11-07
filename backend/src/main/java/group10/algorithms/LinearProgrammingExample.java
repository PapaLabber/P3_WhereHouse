package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

public class LinearProgrammingExample {

    public static void LP() {
      Loader.loadNativeLibraries();
      MPSolver solver = MPSolver.createSolver("GLOP");

      // Problem dimensions 3D
      int warehouses = 2;
      int products = 2;
      int factories = 2;

      double[][][] transportCosts = {
        {{3, 4}, {2, 6}}, // Costs from warehouse 0
        {{5, 2}, {4, 3}}  // Costs from warehouse 1
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
    }

    // Eksempel med Google OR-Tools
    public static void runExample() {
      Loader.loadNativeLibraries();
      MPSolver solver = MPSolver.createSolver("GLOP");

      double infinity = java.lang.Double.POSITIVE_INFINITY;
      // x and y are continuous non-negative variables.
      MPVariable x = solver.makeNumVar(0.0, infinity, "x");
      MPVariable y = solver.makeNumVar(0.0, infinity, "y");
      System.out.println("Number of variables = " + solver.numVariables());

      // x + 2*y <= 14.
      MPConstraint c0 = solver.makeConstraint(-infinity, 14.0, "c0");
      c0.setCoefficient(x, 1);
      c0.setCoefficient(y, 2);

      // 3*x - y >= 0.
      MPConstraint c1 = solver.makeConstraint(0.0, infinity, "c1");
      c1.setCoefficient(x, 3);
      c1.setCoefficient(y, -1);

      // x - y <= 2.
      MPConstraint c2 = solver.makeConstraint(-infinity, 2.0, "c2");
      c2.setCoefficient(x, 1);
      c2.setCoefficient(y, -1);
      System.out.println("Number of constraints = " + solver.numConstraints());

      // Maximize 3 * x + 4 * y.
      MPObjective objective = solver.objective();
      objective.setCoefficient(x, 3);
      objective.setCoefficient(y, 4);
      objective.setMinimization();

      final MPSolver.ResultStatus resultStatus = solver.solve();

      if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
        System.out.println("Solution:");
        System.out.println("Objective value = " + objective.value());
        System.out.println("x = " + x.solutionValue());
        System.out.println("y = " + y.solutionValue());
      } else {
        System.err.println("The problem does not have an optimal solution!");
      }

      System.out.println("\nAdvanced usage:");
      System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
      System.out.println("Problem solved in " + solver.iterations() + " iterations");
    }

    // Chat eksempel 1
  public static void runExample1() {
    Loader.loadNativeLibraries();
    MPSolver solver = MPSolver.createSolver("GLOP");

    // Problem dimensions
    int warehouses = 2;
    int products = 2;
    int factories = 2;

    double[][][] transportCosts = {
      {{3, 4}, {2, 6}}, // Costs from warehouse 0
      {{5, 2}, {4, 3}}  // Costs from warehouse 1
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
          x[w][p][f] = solver.makeNumVar(0.0, infinity, "x[" + w + "][" + p + "][" + f + "]");
        }
      }
    }

    // Capacity constraints per warehouse
    for (int w = 0; w < warehouses; w++) {
      MPConstraint capacityConstraint = solver.makeConstraint(0, warehouseCapacities[w], "capacity_" + w);
      for (int p = 0; p < products; p++) {
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

    // Compatibility constraints
    for (int w = 0; w < warehouses; w++) {
      for (int p = 0; p < products; p++) {
        if (!compatibility[w][p]) {
          for (int f = 0; f < factories; f++) {
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
          objective.setCoefficient(x[w][p][f], transportCosts[w][p][f]);
        }
      }
    }
    objective.setMinimization();

    // Solve
    final MPSolver.ResultStatus resultStatus = solver.solve();

    // Output solution
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Optimal transport cost = " + objective.value());
      for (int w = 0; w < warehouses; w++) {
        for (int p = 0; p < products; p++) {
          for (int f = 0; f < factories; f++) {
            double val = x[w][p][f].solutionValue();
            if (val > 0.0) {
              System.out.printf("x[%d][%d][%d] = %.2f\n", w, p, f, val);
            }
          }
        }
      }
    } else {
      System.err.println("No optimal solution found.");
    }
  }
}
