package group10.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import group10.excel.CapacityRequest;
import group10.excel.RealizedCapacity;
import group10.excel.Result;

/**
 * OR-Tools based allocator for distributing product into warehouse capacity.
 */
@Service
public class WarehouseAllocator {
  /**
   * Allocates pallet requests to warehouse capacity while minimizing transport
   * distance.
   *
   * @param requests    factory requests (demand)
   * @param realisedCap warehouse capacity entries (supply)
   * @return list of Result objects describing final allocations
   */

  // helper method to find transport distances
  public static double[][] computeTransportDistances(List<CapacityRequest> requests,
      List<RealizedCapacity> realisedCap) {
    int R = requests.size();
    int C = realisedCap.size();
    double[][] transportDistances = new double[C][R];

    for (int c = 0; c < C; ++c) {
      double c_x = realisedCap.get(c).getWarehouse().getLongitude();
      double c_y = realisedCap.get(c).getWarehouse().getLatitude();
      for (int r = 0; r < R; ++r) {
        double r_x = requests.get(r).getProductionSite().getLongitude();
        double r_y = requests.get(r).getProductionSite().getLatitude();
        transportDistances[c][r] = Math.hypot(c_x - r_x, c_y - r_y);
      }
    }
    return transportDistances;
  }

  public List<Result> Allocator(List<CapacityRequest> requests, List<RealizedCapacity> realisedCap) {

    Loader.loadNativeLibraries(); // OR-Tools native libs

    int R = requests.size();
    int C = realisedCap.size();

    // Compute Euclidean distance between each warehouse and each production site
    double[][] transportDistances = computeTransportDistances(requests, realisedCap);

    // Create MILP solver
    MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");

    // Decision variables: pallets assigned from request r to warehouse c
    MPVariable[][] x = new MPVariable[R][C];
    for (int r = 0; r < R; ++r) {
      for (int c = 0; c < C; ++c) {
        String name = String.format("x_%d_%d", r, c);

        // Integer variable >= 0, up to demand_r (safe upper bound)
        x[r][c] = solver.makeIntVar(0, requests.get(r).getPalletAmount(), name);

        // Enforce temperature compatibility
        if (realisedCap.get(c).getTemperature() != requests.get(r).getTemperature()) {
          x[r][c].setUb(0.0);
        }
      }
    }

    // Each request must be fully satisfied
    for (int r = 0; r < R; ++r) {
      MPConstraint constraint = solver.makeConstraint(requests.get(r).getPalletAmount(),
          requests.get(r).getPalletAmount(), "demand_" + r);
      for (int c = 0; c < C; ++c) {
        constraint.setCoefficient(x[r][c], 1.0);
      }
    }

    // Warehouse capacity constraints (80% safety factor applied)
    MPConstraint[] constraintCap = new MPConstraint[C];
    for (int c = 0; c < C; ++c) {
      constraintCap[c] = solver.makeConstraint(0.0, realisedCap.get(c).getPalletAmount() * 0.8, "cap_" + c);
      for (int r = 0; r < R; ++r) {
        constraintCap[c].setCoefficient(x[r][c], 1.0);
      }
    }

    // Objective: minimize total distance moved
    MPObjective obj = solver.objective();
    for (int r = 0; r < R; ++r) {
      for (int c = 0; c < C; ++c) {
        obj.setCoefficient(x[r][c], transportDistances[c][r]);
      }
    }

    obj.setMinimization();

    // Solve MILP
    MPSolver.ResultStatus resultStatus = solver.solve();
    if (resultStatus != MPSolver.ResultStatus.OPTIMAL && resultStatus != MPSolver.ResultStatus.FEASIBLE) {
      // Re-set upper bound to be the 100% capacity
      for (int c = 0; c < C; ++c) {
        constraintCap[c].setUb(realisedCap.get(c).getPalletAmount());
      }
      // Resolve
      resultStatus = solver.solve();
    }

    // Check again with new upper bound result
    if (resultStatus != MPSolver.ResultStatus.OPTIMAL && resultStatus != MPSolver.ResultStatus.FEASIBLE) {
      System.err.println("No feasible/optimal solution found: " + resultStatus);
    }

    // Collect final allocations
    List<Result> allocResult = new ArrayList<>();
    for (int r = 0; r < R; ++r) {
      for (int c = 0; c < C; ++c) {
        long val = Math.round(x[r][c].solutionValue());
        if (val > 0) {
          allocResult.add(new Result(realisedCap.get(c).getWarehouse(), realisedCap.get(c).getTemperature(),
              (int) val, requests.get(r)));
        }
      }
    }
    return allocResult;
  }
}
