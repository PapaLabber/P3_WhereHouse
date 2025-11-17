package group10.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import group10.excel.CapacityRequest;
import group10.excel.ProductionSite;
import group10.excel.RealisedCapacity;
import group10.excel.Warehouse;
import group10.excel.Temperature;

public class LinearProgramming {

    static class inputLP {

        int warehouses;
        int products;
        int factories;
        double[][] transportDistances;
        List<CapacityRequest> wantedRequests;
        double[][] warehouseCapacities;

        inputLP(int warehouses, int products, int factories, double[][] transportDistances, List<CapacityRequest> wantedRequests, double[][] warehouseCapacities) {
            this.warehouses = warehouses;
            this.products = products;
            this.factories = factories;
            this.transportDistances = transportDistances;
            this.wantedRequests = wantedRequests;
            this.warehouseCapacities = warehouseCapacities;
        }
    }

    public static void LP(List<CapacityRequest> wantedRequests, List<RealisedCapacity> capacities) {
        int uniqueProductAmount = 3; // products = P notation ---- product amount is a constant 3 (ambient, cold, freeze)

        List<Warehouse> warehouseArray = sortWarehouseArray(capacities); // First we extract a list of the warehouses included in the data of realised capacities.
        List<ProductionSite> siteArray = sortProductionSiteArray(wantedRequests); // Then we extract a list of possible factories from the data of capacity requests.
        int uniqueWarehouseAmount = warehouseArray.size(); // defining the amount of unique warehouse names
        int uniqueFactoryAmount = siteArray.size(); // defining the amount of unique factory names

        // Find distances between warehouses and factories
        double[][] transportDistances = findTransportDistances(warehouseArray, siteArray, uniqueWarehouseAmount, uniqueFactoryAmount);

        double[][] accumulatedWarehouseCapacities = warehouseCapacityMatrix(capacities, uniqueProductAmount, warehouseArray, uniqueWarehouseAmount);

        inputLP objectInputLP = new inputLP(
                uniqueWarehouseAmount, uniqueProductAmount, uniqueFactoryAmount,
                transportDistances, // transportDistances, warehouse 0 to factory 0 and 1 in [0][0-1]
                wantedRequests,
                accumulatedWarehouseCapacities // warehouseCapacities, product 0 (ambient) for warehouse 0 and 1 in [0][0-1]
        );

        /*
     * inputLP objectInputLPtest = new inputLP( 2, 3, 2, new double[][]{ { 3, 4 }, { 5, 2 } }, //
     * transportDistances, warehouse 0 to factory 0 and 1 in [0][0-1] new double[][]{ { 100, 0 }, {
     * 80, 50 }, { 0, 50 } }, // demand, product 0 (ambient) to factory 0 and 1 in [0][0-1] new
     * double[][]{ { 100, 0 }, { 80, 50 }, { 50, 0 } } // warehouseCapacities, product 0 (ambient)
     * for warehouse 0 and 1 in [0][0-1] );
         */
        // oldLP();
    }

    private static double[][] warehouseCapacityMatrix(List<RealisedCapacity> capacities, int uniqueProductAmount, List<Warehouse> warehouseArray, int uniqueWarehouseAmount) {
        double[][] matrix = new double[uniqueProductAmount][uniqueWarehouseAmount];

        // map warehouse name -> column index
        Map<String, Integer> indexByName = new LinkedHashMap<>();
        for (int i = 0; i < uniqueWarehouseAmount; i++) {
            indexByName.put(warehouseArray.get(i).getName(), i);
        }

        // accumulate palletAmount per (product, warehouse)
        for (RealisedCapacity rc : capacities) {
            String name = rc.getWarehouse().getName();
            Integer col = indexByName.get(name);
            int amount = rc.getPalletAmount();
            Temperature temp = rc.getTemperature();
            switch (temp) {
                case AMBIENT:
                    matrix[0][col] += amount;
                    break;
                case COLD:
                    matrix[1][col] += amount;
                    break;
                case FREEZE:
                    matrix[2][col] += amount;
                    break;
                default:
                    // unknown temperature: ignore
                    break;
            }
        }
        return matrix;
    }

    private static List<Warehouse> sortWarehouseArray(List<RealisedCapacity> capacities) {
        List<Warehouse> warehouseArray = new ArrayList<>();

        for (RealisedCapacity warehouse : capacities) {
            Warehouse W = Warehouse.fromName(warehouse.getWarehouse().getName());
            if (warehouseArray.contains(W) == false) {
                warehouseArray.add(W);
                System.out.println("W: " + W);
            }
        }
        return warehouseArray;
    }

    private static List<ProductionSite> sortProductionSiteArray(
            List<CapacityRequest> wantedRequests) {
        List<ProductionSite> siteArray = new ArrayList<>();

        for (CapacityRequest factory : wantedRequests) {
            ProductionSite F = ProductionSite.fromName(factory.getProductionSite().getName());
            if (siteArray.contains(F) == false) {
                siteArray.add(F);
                System.out.println(
                        "F: name: " + F.getName() + " long: " + F.getLongitude() + " lat: " + F.getLatitude());
            }
        }
        return siteArray;
    }

    private static double[][] findTransportDistances(List<Warehouse> warehouseArray, List<ProductionSite> siteArray, int uniqueWarehouseAmount, int uniqueFactoryAmount) {
        double[][] transportDistances = new double[uniqueWarehouseAmount][uniqueFactoryAmount]; // transportDistances = T_{w,f} notation

        for (int w = 0; w < uniqueWarehouseAmount; w++) {
            for (int f = 0; f < uniqueFactoryAmount; f++) {
                double longtitudeW = warehouseArray.get(w).getLongitude();
                double latitudeW = warehouseArray.get(w).getLatitude();
                double longtitudeF = siteArray.get(f).getLongitude();
                double latitudeF = siteArray.get(f).getLatitude();
                transportDistances[w][f] = Math.sqrt(Math.pow(longtitudeW - longtitudeF, 2) + Math.pow(latitudeW - latitudeF, 2));
                System.out.println(warehouseArray.get(w) + "->" + siteArray.get(f) + " dist:" + transportDistances[w][f]);
            }
        }
        return transportDistances;
    }

    public static void oldLP() {
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
            {3, 4}, // warehouse 0 to factory 0 and 1
            {5, 2} // warehouse 1 to factory 0 and 1
        };

        double[][] demand = { // demand = D_{p,f} notation
            {50, 0}, // product 0 (ambient) to factory 0 and 1
            {40, 25}, // product 1 (cold) to factory 0 and 1
            {0, 25} // product 2 (freeze) to factory 0 and 1
        };

        double[][] warehouseCapacities = { // warehouseCapacities = C_{p,w} notation
            {100, 0}, // product 0 (ambient) for warehouse 0 and 1
            {80, 50}, // product 1 (cold) for warehouse 0 and 1
            {50, 0} // product 2 (freeze) for warehouse 0 and 1
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
                MPConstraint capacityConstraint
                        = solver.makeConstraint(0.0, capacity, "capacity_p" + p + "_w" + w);
                for (int f = 0; f < factories; f++) {
                    capacityConstraint.setCoefficient(x[w][p][f], 1);
                }
            }
        }

        // Demand constraints per product and factory
        for (int p = 0; p < products; p++) {
            for (int f = 0; f < factories; f++) {
                // Tight constraint: supply = demand.
                MPConstraint demandConstraint
                        = solver.makeConstraint(demand[p][f], demand[p][f], "demand_" + p + "_" + f);
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
           * setCoefficent sets a number to be multiplied upon our x, this number is the transport
           * distance from warehouse w to factory f as an example: transportDistances[w][f] *
           * x[w][p][f]
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
    }

}
