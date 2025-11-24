package group10.algorithms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPConstraint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

import java.io.*;
import java.util.*;


public class WarehouseAllocator {
    static class Request {
        int palletAmount;           
        String temperature;         
        String productionSite;      
        int ID;                     
        int year;                
    }
    static class Warehouse {
        int palletAmount;
        Set<String> temperature;
        String warehouse;
        int year;
        double x; 
        double y;
    }


    public void Allocator() {
        System.out.println("***Allocator started***"); // TODO: Remove

        Loader.loadNativeLibraries(); // OR-Tools native libs

        // --- 1) Parse rows: build lists of requests and warehouses ---
        List<Request> requests = new ArrayList<>();
        List<Warehouse> warehouses = new ArrayList<>();
        Map<String, Integer> warehouseIndexById = new HashMap<>();

        // ---- For demo: create dummy warehouses (replace with real reads) ----
        Warehouse w1 = new Warehouse(); w1.palletAmount = 200; w1.temperature = new HashSet<>(Arrays.asList("Ambient")); w1.warehouse = "WH_A"; w1.year = 2026; w1.x=0; w1.y=0; warehouses.add(w1);
        Warehouse w2 = new Warehouse(); w2.palletAmount = 140; w2.temperature = new HashSet<>(Arrays.asList("Freeze")); w2.warehouse = "WH_B"; w2.year = 2026; w2.x=10; w2.y=5; warehouses.add(w2);
        Warehouse w3 = new Warehouse(); w3.palletAmount = 200; w3.temperature = new HashSet<>(Arrays.asList("Ambient")); w3.warehouse = "WH_C"; w3.year = 2026; w3.x=2; w3.y=3; warehouses.add(w3);
        
        // TODO: remove?
        for (int wi=0; wi<warehouses.size(); ++wi) {
            warehouseIndexById.put(warehouses.get(wi).warehouse, wi);
        }

        // ---- For demo: create dummy requests (replace with real reads) ----
        Request r1 = new Request(); r1.palletAmount=300; r1.temperature="Ambient"; r1.productionSite="Hillerød"; r1.ID=1; r1.year=2026; requests.add(r1);
        Request r2 = new Request(); r2.palletAmount=100; r2.temperature="Ambient"; r2.productionSite="Kalundborg"; r2.ID=2; r2.year=2026; requests.add(r2);
        Request r3 = new Request(); r3.palletAmount=140; r3.temperature="Freeze"; r3.productionSite="Kalundborg"; r3.ID=3; r3.year=2026; requests.add(r3);

        int I = requests.size();
        int W = warehouses.size();

        // TODO: remove
        System.out.println("w1: palletCapacity=" + w1.palletAmount + ", temp=" + w1.temperature + ", warehouse=" + w1.warehouse + ", year=" + w1.year + "Coord x=" + w1.x + "Coord y=" + w1.y);
        System.out.println("w2: palletCapacity=" + w2.palletAmount + ", temp=" + w2.temperature + ", warehouse=" + w2.warehouse + ", year=" + w2.year + "Coord x=" + w2.x + "Coord y=" + w2.y);
        System.out.println("r1: palletRequest=" + r1.palletAmount + ", temp=" + r1.temperature + ", site=" + r1.productionSite + ", ID=" + r1.ID + ", year=" + r1.year);
        System.out.println("r2: palletRequest=" + r2.palletAmount + ", temp=" + r2.temperature + ", site=" + r2.productionSite + ", ID=" + r2.ID + ", year=" + r2.year);
        
        // --- 2) Build distance matrix (placeholder Euclidean on x/y) ---
        double[][] dist = new double[W][I];
        Map<String, Integer> factoryIndex = new HashMap<>();
        // If you have factory coords, compute; else create placeholder distances:
        for (int wi=0; wi<W; ++wi) {
            for (int i=0; i<I; ++i) {
                // Placeholder: distance = hypot(warehouse.x - factoryIndex, warehouse.y - i)
                // You should replace with real distances later.
                dist[wi][i] = Math.hypot(warehouses.get(wi).x - (i*3), warehouses.get(wi).y - (i*2));
            }
        }

        // TODO: Remove
        System.out.println("---Distance Matrix (Request x Warehouse)---");
        // Print header (warehouse indices)
        System.out.print("Request\\Warehouse");
        for (int w = 0; w < W; w++) {
            System.out.printf("%15s", "W" + w);
        }
        System.out.println();
        // Print rows
        for (int i = 0; i < I; i++) {
            System.out.printf("R%d%14s", i, "");
            for (int w = 0; w < W; w++) {
                System.out.printf("%15.2f", dist[i][w]);
            }
            System.out.println();
        }

        // --- 3) Build MIP model ---
        MPSolver solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) {
            //throw new Exception("Could not create solver (check OR-Tools installation)");
        }

        // decision variable: x[i][w]
        // Example:
        // Request #3 needs 150 pallets. Warehouse B can take 80.
        // If the solver sets x[3][B] = 80, it means “allocate 80 pallets of request #3 to warehouse B”.
        MPVariable[][] x = new MPVariable[I][W];
        for (int i=0;i<I;++i) {
            for (int w=0; w<W; ++w) {
                String name = String.format("x_%d_%d", i, w);
                // Integer variable >= 0, up to demand_i (safe upper bound)
                x[i][w] = solver.makeIntVar(0, requests.get(i).palletAmount, name);
                // Enforce temperature compatibility: if not compatible, set upper bound 0
                if (!warehouses.get(w).temperature.contains(requests.get(i).temperature)) {
                    x[i][w].setUb(0.0);
                }
            }
        }

        // TODO: Remove
        System.out.println("---Decision Variables (Request x Warehouse)---");
        // Print header (warehouse indices)
        System.out.print("Request\\Warehouse");
        for (int w = 0; w < W; w++) {
            System.out.printf("%15s", "W" + w);
        }
        System.out.println();
        // Print rows
        for (int i = 0; i < I; i++) {
            System.out.printf("R%d%14s", i, "");
            for (int w = 0; w < W; w++) {
                System.out.printf("%15s", x[i][w].name());
            }
            System.out.println();
        }

        // Request demands: sum_w x[i][w] == demand_i
        for (int i=0;i<I;++i) {
            MPConstraint c = solver.makeConstraint(requests.get(i).palletAmount, requests.get(i).palletAmount, "demand_"+i);
            for (int w=0; w<W; ++w) 
                c.setCoefficient(x[i][w], 1.0);
        }

        // Warehouse capacities: sum_i x[i][w] <= capacity_w
        for (int w=0; w<W; ++w) {
            MPConstraint c = solver.makeConstraint(0.0, warehouses.get(w).palletAmount, "cap_"+w);
            for (int i=0;i<I;++i) 
                c.setCoefficient(x[i][w], 1.0);
        }

        // Objective: minimize sum_{i,w} dist[w][i] * x[i][w]
        MPObjective obj = solver.objective();
        for (int i=0;i<I;++i)
            for (int w=0; w<W; ++w)
                obj.setCoefficient(x[i][w], dist[w][i]);
        
        // TODO: Remove 
        // Print objective coefficient matrix (rows = Request i, cols = Warehouse w)
        System.out.println("---Objective Coefficients (Request x Warehouse)---");
        System.out.print("Request\\Warehouse");
        for (int i = 0; i < I; i++) {
            System.out.printf("%15s", "W" + i);
        }
        System.out.println();
        for (int i = 0; i < I; i++) {
            System.out.printf("R%d%13s", i, "");
            for (int w = 0; w < W; w++) {
                System.out.printf("%15.2f", obj.getCoefficient(x[i][w])); // coefficient used for x[i][w]
            }
            System.out.println();
        }

        obj.setMinimization();



        // --- 4) Solve ---
        final MPSolver.ResultStatus resultStatus = solver.solve();
        if (resultStatus != MPSolver.ResultStatus.OPTIMAL && resultStatus != MPSolver.ResultStatus.FEASIBLE) {
            System.err.println("No feasible/optimal solution found: " + resultStatus);
            return;
        }

        System.out.println("Objective: " + obj.value());

        // --- 5) Collect and write allocations back to Excel ---
        // For each request i, generate strings like "WH_A_50;WH_B_150" etc.
        for (int i=0;i<I;++i) {
            List<String> parts = new ArrayList<>();
            for (int w=0; w<W; ++w) {
                long val = Math.round(x[i][w].solutionValue());
                if (val > 0) parts.add(warehouses.get(w).warehouse + "_" + val);
            }
            String out = String.join(";", parts);
            System.out.println("Request " + requests.get(i).ID + " -> " + out);
            // Write `out` into the Excel sheet at requests.get(i).excelRowIndex in a new column.
            // (You must map request->row index when reading.)
        }
    }
}

