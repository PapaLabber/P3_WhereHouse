package group10.algorithms;

import java.util.ArrayList;
import java.util.List;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import group10.excel.CapacityRequest;
import group10.excel.ProductionSite;
import group10.excel.RealisedCapacity;
import group10.excel.Warehouse;

public class LinearProgramming {

 static class inputLP {
        int warehouses;
        int products;
        int factories;
        double[][] transportDistances;
        double[][] demand;
        double[][] warehouseCapacities;

        inputLP(int warehouses, int products, int factories,
                double[][] transportDistances, double[][] demand,
                double[][] warehouseCapacities) {

            this.warehouses = warehouses;
            this.products = products;
            this.factories = factories;
            this.transportDistances = transportDistances;
            this.demand = demand;
            this.warehouseCapacities = warehouseCapacities;

        }
    }


  public static void LP(List<CapacityRequest> wantedRequests, List<RealisedCapacity> capacities) {
    int warehouses = capacities.size(); // warehouses = W notation --- is dynamic but will initialize to capacities.size should be changed later
    int products = 3; // products = P notation ---- product amount is a constant 3 (ambient, cold, freeze)
    int factories = 5; // factories = F notation ---- is dynamic but will initialize to 5 should be changed later

    double[][] transportDistances = new double[warehouses][factories]; // transportDistances = T_{w,f} notation
    List<Warehouse> warehouseArray = new ArrayList<>();
    List<ProductionSite> siteArray = new ArrayList<>();
    for (RealisedCapacity warehouse : capacities) {
      Warehouse W = Warehouse.fromName(warehouse.getWarehouse().getName());
      if (warehouseArray.contains(W) == false) {
        warehouseArray.add(W);
        
      }
      System.out.println("W: "+warehouse);
    }

    for (CapacityRequest factory : wantedRequests) {
      ProductionSite F = ProductionSite.fromName(factory.getProductionSite().getName());

      if (siteArray.contains(F) == false) {
        siteArray.add(F);
        System.out.println("F: name: "+F.getName()+" long: "+F.getLongitude()+" lat: "+F.getLatitude());
      }
    }
    
    warehouses = warehouseArray.size(); // updating warehouses to fit the dynamic amount for the year
    factories = siteArray.size(); // updating factories to fit the dynamic amount for the year

    for (int w = 0; w < warehouses; w++) { // DER ER FEJL HER <-------- !!!!!!!!!!!!!!!!!!!!!! VI ER OGSÅ NOGET TIL HER !!!!!!!!!!!!!!!!!!!!
      for (int f = 0; f < factories; f++) {
        double longtitudeW = warehouseArray.get(w).getLongitude();
        double latitudeW = warehouseArray.get(w).getLatitude();
        double longtitudeF = siteArray.get(f).getLongitude();
        double latitudeF = siteArray.get(f).getLatitude();
        transportDistances[w][f] = Math.sqrt(Math.pow(longtitudeW-longtitudeF,2)+Math.pow(latitudeW-latitudeF,2));
        //System.out.println(warehouseArray.get(w)+"->"+w+" "+f+siteArray.get(f)+" dist:"+transportDistances[w][f]);
      }
    }

/* 
    for (RealisedCapacity cap : capacities) {
        System.out.println(cap);
    }
        
    for(CapacityRequest req : wantedRequests) {
          System.out.println(req);
    }*/
  

    inputLP objectInputLP = new inputLP(
        2, 3, 2,
        new double[][]{ { 3, 4 }, { 5, 2 } },                // transportDistances, warehouse 0 to factory 0 and 1 in [0][0-1]
        new double[][]{ { 100, 0 }, { 80, 50 }, { 0, 50 } }, // demand, product 0 (ambient) to factory 0 and 1 in [0][0-1]
        new double[][]{ { 100, 0 }, { 80, 50 }, { 50, 0 } } // warehouseCapacities, product 0 (ambient) for warehouse 0 and 1 in [0][0-1]
    );


    
    
    
    //oldLP();
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
  }

}
