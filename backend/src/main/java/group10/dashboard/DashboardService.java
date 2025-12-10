package group10.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import group10.excel.RealizedCapacity;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;

/**
 * Service that aggregates allocation results and capacity data
 * into dashboard-friendly Data Transfer Objects DTOs.
 */
@Service
public class DashboardService {
    public static class WarehouseDashboard {

        // Instance fields
        private final String warehouseName;
        private final int totalCapacity;
        private final int usedCapacity;
        private final int remainingCapacity;
        private final double utilisationPercent;
        private final int ambient;
        private final int cold;
        private final int freeze;

        // Constructor
        public WarehouseDashboard(String warehouseName, int totalCapacity, int usedCapacity,
                int remainingCapacity, double utilisationPercent,
                int ambient, int cold, int freeze) {
            this.warehouseName = warehouseName;
            this.totalCapacity = totalCapacity;
            this.usedCapacity = usedCapacity;
            this.remainingCapacity = remainingCapacity;
            this.utilisationPercent = utilisationPercent;
            this.ambient = ambient;
            this.cold = cold;
            this.freeze = freeze;
        }

        // Getters
        public String getWarehouseName() {
            return this.warehouseName;
        }

        public int getTotalCapacity() {
            return this.totalCapacity;
        }

        public int getUsedCapacity() {
            return this.usedCapacity;
        }

        public int getRemainingCapacity() {
            return this.remainingCapacity;
        }

        public double getUtilisationPercent() {
            return this.utilisationPercent;
        }

        public int getAmbient() {
            return this.ambient;
        }

        public int getCold() {
            return this.cold;
        }

        public int getFreeze() {
            return this.freeze;
        }
    }

    /**
     * Builds dashboard values for each warehouse based on available capacity
     * and allocated storage results.
     * 
     * @param capacities amount of available space
     * @param results    space used
     * @return a list of WarehouseDashboard objects containing the capacity usage
     *         for each warehouse
     */
    public List<WarehouseDashboard> buildDashboard(List<RealizedCapacity> capacities,
            List<Result> results) {

        // Guard against null inputs by replacing them with empty lists
        if (capacities == null) {
            capacities = Collections.emptyList();
        }
        if (results == null) {
            results = Collections.emptyList();
        }

        // Compute total pallet capacity per warehouse from realized capacities
        Map<Warehouse, Integer> totalCapacityByWarehouse = capacities.stream()
                .collect(Collectors.groupingBy(
                        RealizedCapacity::getWarehouse,
                        Collectors.summingInt(RealizedCapacity::getPalletAmount)));

        // Prepare structures for:
        // - total used capacity per warehouse
        // - temperature breakdown (ambient/cold/freeze) per warehouse
        Map<Warehouse, Integer> usedByWarehouse = new HashMap<>();
        Map<Warehouse, Map<Temperature, Integer>> tempBreakdown = new HashMap<>();

        // Aggregate used capacity and temperature breakdown from allocation results
        for (Result r : results) {
            Warehouse w = r.getWarehouse();
            Temperature t = r.getTemperature();
            int amount = r.getAmountStored();

            // Accumulate total used pallets per warehouse
            usedByWarehouse.merge(w, amount, Integer::sum);

            // Accumulate used pallets per temperature zone for each warehouse
            tempBreakdown
                    .computeIfAbsent(w, __ -> new EnumMap<>(Temperature.class))
                    .merge(t, amount, Integer::sum);
        }

        // Build DTOs for each warehouse that has a defined capacity
        List<WarehouseDashboard> dtoList = new ArrayList<>();

        for (Map.Entry<Warehouse, Integer> entry : totalCapacityByWarehouse.entrySet()) {
            Warehouse warehouse = entry.getKey();
            int totalCapacity = entry.getValue();

            // Look up how much of that capacity is used (default to 0 if no results)
            int used = usedByWarehouse.getOrDefault(warehouse, 0);

            // Remaining capacity cannot be negative
            int remaining = Math.max(0, totalCapacity - used);

            // Remaining capacity cannot be negative
            double utilisation = totalCapacity == 0
                    ? 0.0
                    : (used * 100.0) / totalCapacity;

            // Remaining capacity cannot be negative
            Map<Temperature, Integer> tempMap = tempBreakdown.getOrDefault(warehouse, Collections.emptyMap());

            int ambient = tempMap.getOrDefault(Temperature.AMBIENT, 0);
            int cold = tempMap.getOrDefault(Temperature.COLD, 0);
            int freeze = tempMap.getOrDefault(Temperature.FREEZE, 0);

            // Create a dashboard DTO summarizing this warehouse's utilisation
            dtoList.add(new WarehouseDashboard(
                    warehouse.getName(),
                    totalCapacity,
                    used,
                    remaining,
                    utilisation,
                    ambient,
                    cold,
                    freeze));
        }

        // Sort warehouses by utilisation percentage in descending order
        dtoList.sort(Comparator.comparingDouble(WarehouseDashboard::getUtilisationPercent).reversed());

        // Return the dashboard data to be consumed by the API/controller
        return dtoList;
    }
}
