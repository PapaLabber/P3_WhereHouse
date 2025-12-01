package group10.app;

import group10.excel.RealizedCapacity;
import group10.excel.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory holder for the last allocation run.
 * This is not persistent and will reset if the application restarts.
 */
@Component
public class AllocationState {

    private List<RealizedCapacity> lastCapacities = Collections.emptyList();
    private List<Result> lastResults = Collections.emptyList();

    public synchronized void update(List<RealizedCapacity> capacities, List<Result> results) {
        this.lastCapacities = capacities != null ? capacities : Collections.emptyList();
        this.lastResults = results != null ? results : Collections.emptyList();
    }

    public synchronized List<RealizedCapacity> getLastCapacities() {
        return lastCapacities;
    }

    public synchronized List<Result> getLastResults() {
        return lastResults;
    }

    public synchronized boolean hasData() {
        return !lastCapacities.isEmpty() && !lastResults.isEmpty();
    }
}
