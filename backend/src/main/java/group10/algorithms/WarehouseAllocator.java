package group10.algorithms;

import java.util.ArrayList;
import java.util.List;

import javax.naming.spi.DirStateFactory;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import group10.excel.CapacityRequest;
import group10.excel.RealisedCapacity;
import group10.excel.Result;

public class WarehouseAllocator {

    public String Allocator(List<CapacityRequest> requests, List<RealisedCapacity> realisedCap) {

        Loader.loadNativeLibraries(); // OR-Tools native libs

        //We want the size of requests and realisedCap(realised capacity) because we see them as single warehouse capacities and requests
        int R = requests.size();
        int C = realisedCap.size();

        double[][] transportDistances = new double[C][R];
        for (int c = 0; c < C; ++c) {
            for (int r = 0; r < R; ++r) {
                transportDistances[c][r] = Math.sqrt(
                        Math.pow(realisedCap.get(c).getWarehouse().getLongitude() - requests.get(r).getProductionSite().getLongitude(), 2)
                        + Math.pow(realisedCap.get(c).getWarehouse().getLatitude() - requests.get(r).getProductionSite().getLatitude(), 2));
            }
        }

        // --- 3) Build MIP model ---
        MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) {
            //throw new Exception("Could not create solver (check OR-Tools installation)");
        }

        // decision variable: x[r][c]
        // Example:
        // Request #3 needs 150 pallets. Warehouse B can take 80.
        // If the solver sets x[3][B] = 80, it means “allocate 80 pallets of request #3 to warehouse B”.
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

        // TODO: Remove
        System.out.println("---Decision Variables (Request x Warehouse)---");
        // Print header (warehouse indices)
        System.out.print("Request\\Warehouse");
        for (int c = 0; c < C; c++) {
            System.out.printf("%15s", realisedCap.get(c).getWarehouse().getName() + "(" + realisedCap.get(c).getTemperature() + ")");
        }
        System.out.println();
        // Print rows
        for (int r = 0; r < R; r++) {
            System.out.printf("R%14s", requests.get(r).getID() + "(" + requests.get(r).getTemperature() + ")", "");
            for (int c = 0; c < C; c++) {
                System.out.printf("%15s", x[r][c].name());
            }
            System.out.println();
        }

        // Request demands: sum_c x[r][c] == demand_r
        for (int r = 0; r < R; ++r) {
            MPConstraint constraint = solver.makeConstraint(requests.get(r).getPalletAmount(), requests.get(r).getPalletAmount(), "demand_" + r);
            for (int c = 0; c < C; ++c) {
                constraint.setCoefficient(x[r][c], 1.0);
            }
        }

        // Warehouse capacities: sum_r x[r][c] <= capacity_c
        for (int c = 0; c < C; ++c) {
            MPConstraint constraint = solver.makeConstraint(0.0, realisedCap.get(c).getPalletAmount(), "cap_" + c);
            for (int r = 0; r < R; ++r) {
                constraint.setCoefficient(x[r][c], 1.0);
            }
        }

        // Objective: minimize sum_{r,c} dist[c][r] * x[r][c]
        MPObjective obj = solver.objective();
        for (int r = 0; r < R; ++r) {
            for (int c = 0; c < C; ++c) {
                obj.setCoefficient(x[r][c], transportDistances[c][r]);
            }
        }

        // TODO: Remove 
        // Print objective coefficient matrix (rows = request r, cols = realisedCap c)
        System.out.println("---Objective Coefficients (Request x Warehouse)---");
        System.out.print("Request\\Warehouse");
        for (int c = 0; c < C; c++) {
            System.out.printf("%15s", realisedCap.get(c).getWarehouse().getName()+"(" + realisedCap.get(c).getTemperature() + ")");
        }
        System.out.println();
        for (int r = 0; r < R; r++) {
            System.out.printf("R%13s", requests.get(r).getID() + "(" + requests.get(r).getTemperature() + ")", "");
            for (int c = 0; c < C; c++) {
                System.out.printf("%15.2f", obj.getCoefficient(x[r][c])); // coefficient used for x[r][c]
            }
            System.out.println();
        }

        obj.setMinimization();

        // --- 4) Solve ---
        final MPSolver.ResultStatus resultStatus = solver.solve();
        if (resultStatus != MPSolver.ResultStatus.OPTIMAL && resultStatus != MPSolver.ResultStatus.FEASIBLE) {
            System.err.println("No feasible/optimal solution found: " + resultStatus);
            return resultStatus.toString();
        }

        System.out.println("Objective: " + obj.value());

        // --- 5) Collect and write allocations back to Excel ---
        // For each request r, generate strings like "WH_A_50;WH_B_150" etc.

        List<Result> allocResult = new ArrayList<>();
        for (int r = 0; r < R; ++r) {
            List<String> allocList = new ArrayList<>();
            for (int c = 0; c < C; ++c) {
                long val = Math.round(x[r][c].solutionValue());
                if (val > 0) {
                    allocList.add(realisedCap.get(c).getWarehouse() + "_" + realisedCap.get(c).getTemperature() + "_" + val);
                    allocResult.add(new Result(realisedCap.get(c).getWarehouse(), realisedCap.get(c).getTemperature(), (int)val, requests.get(r)));
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

        
        return resultStatus.toString();
    }
}
