package group10.dashboard;

import group10.excel.RealizedCapacity;
import group10.excel.Result;
import group10.excel.Temperature;
import group10.excel.Warehouse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that aggregates allocation results and capacity data
 * into dashboard-friendly Data Transfer Objects DTOs.
 */
@Service
public class DashboardService {

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

            Map<Temperature, Integer> tempMap =
                    tempBreakdown.getOrDefault(warehouse, Collections.emptyMap());

            TemperatureDashboard temperatureDashboard = TemperatureDashboard.fromMap(tempMap);

            dtoList.add(new WarehouseDashboard(
                    warehouse.getName(),   // or warehouse.toString()
                    totalCapacity,
                    used,
                    remaining,
                    utilisation,
                    temperatureDashboard
            ));
        }

        
        dtoList.sort(Comparator.comparingDouble(WarehouseDashboard::getUtilisationPercent).reversed());

        return dtoList;
    }
}
