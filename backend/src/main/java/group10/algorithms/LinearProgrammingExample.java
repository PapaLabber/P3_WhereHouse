package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

public class LinearProgrammingExample {

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
}
