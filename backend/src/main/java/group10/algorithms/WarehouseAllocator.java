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

@Service // TODO: Forklar nærmere (Philippe)
public class WarehouseAllocator {

  public List<Result> Allocator(List<CapacityRequest> requests, List<RealizedCapacity> realisedCap) {

    Loader.loadNativeLibraries(); // OR-Tools native libs

    // We want the size of requests and realisedCap(realised capacity) because we
    // see them as single warehouse capacities and requests
    int R = requests.size();
    int C = realisedCap.size();

    // -- 1) Find transport distances --
    double[][] transportDistances = new double[C][R];
    for (int c = 0; c < C; ++c) {
      for (int r = 0; r < R; ++r) {
        transportDistances[c][r] = Math.sqrt(
            Math.pow(
                realisedCap.get(c).getWarehouse().getLongitude() - requests.get(r).getProductionSite().getLongitude(),
                2)
                + Math.pow(
                    realisedCap.get(c).getWarehouse().getLatitude() - requests.get(r).getProductionSite().getLatitude(),
                    2));
      }
    }

    MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");

    // -- 2) compatibility variable matrix: x[r][c] --
    MPVariable[][] x = new MPVariable[R][C];
    for (int r = 0; r < R; ++r) {
      for (int c = 0; c < C; ++c) {
        String name = String.format("x_%d_%d", r, c);
        // Integer variable >= 0, up to demand_r (safe upper bound)
        x[r][c] = solver.makeIntVar(0, requests.get(r).getPalletAmount(), name);
        // Enforce temperature compatibility: if not compatible, set upper bound 0
        if (realisedCap.get(c).getTemperature() != requests.get(r).getTemperature()) {
          x[r][c].setUb(0.0);
        }
      }
    }

    // -- 3) Request demands: sum_c x[r][c] == demand_r --
    for (int r = 0; r < R; ++r) {
      MPConstraint constraint = solver.makeConstraint(requests.get(r).getPalletAmount(),
          requests.get(r).getPalletAmount(), "demand_" + r);
      for (int c = 0; c < C; ++c) {
        constraint.setCoefficient(x[r][c], 1.0);
      }
    }

    // -- 4) Warehouse capacities: sum_r x[r][c] <= capacity_c --
    for (int c = 0; c < C; ++c) {
      MPConstraint constraint = solver.makeConstraint(0.0, realisedCap.get(c).getPalletAmount(), "cap_" + c);
      for (int r = 0; r < R; ++r) {
        constraint.setCoefficient(x[r][c], 1.0);
      }
    }

    // -- 5) Objective: minimize sum_{r,c} dist[c][r] * x[r][c] --
    MPObjective obj = solver.objective();
    for (int r = 0; r < R; ++r) {
      for (int c = 0; c < C; ++c) {
        obj.setCoefficient(x[r][c], transportDistances[c][r]);
      }
    }

    obj.setMinimization();

    // --- 6) Solve ---
    final MPSolver.ResultStatus resultStatus = solver.solve();
    if (resultStatus != MPSolver.ResultStatus.OPTIMAL && resultStatus != MPSolver.ResultStatus.FEASIBLE) {
      System.err.println("No feasible/optimal solution found: " + resultStatus);
    }

    System.out.println("Objective: " + obj.value());

    // --- 7) Collect and write allocations to a list of result objects---
    List<Result> allocResult = new ArrayList<>();
    for (int r = 0; r < R; ++r) {
      List<String> allocList = new ArrayList<>();
      for (int c = 0; c < C; ++c) {
        long val = Math.round(x[r][c].solutionValue());
        if (val > 0) {
          allocList.add(realisedCap.get(c).getWarehouse() + "_" + realisedCap.get(c).getTemperature() + "_" + val);
          allocResult.add(new Result(realisedCap.get(c).getWarehouse(), realisedCap.get(c).getTemperature(), (int) val,
              requests.get(r)));
        }
      }
      String out = String.join(";", allocList);
      System.out.println("Request " + requests.get(r).getID() + " " + requests.get(r).getTemperature() + " -> " + out);
    }
    System.out.println("--- Full Allocations ---");
    for (Result res : allocResult) {
      System.out.println(res.toString());
    }
    System.out.println();
    System.out.println();

    return allocResult;
  }
}
