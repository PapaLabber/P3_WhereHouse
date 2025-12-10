package group10.excel;

import java.util.List;

public class FilterFiller {
    private List<String> names;
    private List<Integer> values;

    public FilterFiller(List<String> names, List<Integer> values) {
        this.names = names;
        this.values = values;
    }

    public List<String> getNames() { return names; }
    public List<Integer> getValues() { return values; }
}
