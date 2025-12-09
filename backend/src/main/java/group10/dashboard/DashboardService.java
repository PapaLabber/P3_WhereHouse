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

        private final String warehouseName;
        private final int totalCapacity;
        private final int usedCapacity;
        private final int remainingCapacity;
        private final double utilisationPercent;
        private final int ambient;
        private final int cold;
        private final int freeze;

        public WarehouseDashboard(String warehouseName,int totalCapacity,int usedCapacity,
                                  int remainingCapacity,double utilisationPercent,
                                  int ambient,int cold,int freeze
        ) 
        {
                this.warehouseName = warehouseName;
                this.totalCapacity = totalCapacity;
                this.usedCapacity = usedCapacity;
                this.remainingCapacity = remainingCapacity;
                this.utilisationPercent = utilisationPercent;
                this.ambient = ambient;
                this.cold = cold;
                this.freeze = freeze;
        }
        public String getWarehouseName() {return this.warehouseName; }
        public int getTotalCapacity() {return this.totalCapacity; }
        public int getUsedCapacity() {return this.usedCapacity; }
        public int getRemainingCapacity() {return this.remainingCapacity; }
        public double getUtilisationPercent() {return this.utilisationPercent; }
        public int getAmbient() {return this.ambient; }
        public int getCold() {return this.cold; }
        public int getFreeze() {return this.freeze; }
    }




    /**
     * Build the dashboard view from the realised capacities (total capacity)
     * and allocation results (used capacity).
     */
    public List<WarehouseDashboard> buildDashboard(List<RealizedCapacity> capacities,
                                                   List<Result> results) {

        if (capacities == null) {
            capacities = Collections.emptyList();
        }
        if (results == null) {
            results = Collections.emptyList();
        }

        // 1) Total capacity per warehouse
        Map<Warehouse, Integer> totalCapacityByWarehouse = capacities.stream()
                .collect(Collectors.groupingBy(
                        RealizedCapacity::getWarehouse,
                        Collectors.summingInt(RealizedCapacity::getPalletAmount)
                ));

        // 2) Used capacity per warehouse + per temperature
        Map<Warehouse, Integer> usedByWarehouse = new HashMap<>();
        Map<Warehouse, Map<Temperature, Integer>> tempBreakdown = new HashMap<>();

        for (Result r : results) {
            Warehouse w = r.getWarehouse();
            Temperature t = r.getTemperature();
            int amount = r.getAmountStored();

            usedByWarehouse.merge(w, amount, Integer::sum);

            tempBreakdown
                    .computeIfAbsent(w, __ -> new EnumMap<>(Temperature.class))
                    .merge(t, amount, Integer::sum);
        }

        // 3) Build DTOs for each warehouse that has capacity
        List<WarehouseDashboard> dtoList = new ArrayList<>();

        for (Map.Entry<Warehouse, Integer> entry : totalCapacityByWarehouse.entrySet()) {
            Warehouse warehouse = entry.getKey();
            int totalCapacity = entry.getValue();
            int used = usedByWarehouse.getOrDefault(warehouse, 0);
            int remaining = Math.max(0, totalCapacity - used);

            double utilisation = totalCapacity == 0
                    ? 0.0
                    : (used * 100.0) / totalCapacity;
            Map<Temperature, Integer> tempMap = tempBreakdown.getOrDefault(warehouse, Collections.emptyMap());

            int ambient = tempMap.getOrDefault(Temperature.AMBIENT, 0);
            int cold    = tempMap.getOrDefault(Temperature.COLD, 0);
            int freeze  = tempMap.getOrDefault(Temperature.FREEZE, 0);

            dtoList.add(new WarehouseDashboard(
                    warehouse.getName(),
                    totalCapacity,
                    used,
                    remaining,
                    utilisation,
                    ambient,
                    cold,
                    freeze
            ));
        }

        dtoList.sort(Comparator.comparingDouble(WarehouseDashboard::getUtilisationPercent).reversed());

        return dtoList;
    }
}
