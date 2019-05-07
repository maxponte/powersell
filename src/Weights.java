import java.util.*;

public class Weights {
    List<NavigableMap<Double, Double>> weightByOption;
    Weights(int K) {
        weightByOption = new ArrayList<>();
        for (int n = 1; n <= K; n++) {
            weightByOption.add(new TreeMap<>());
        }
    }
    Double get(int n, double budget) {
        if(n == 0) return 0.0;
        NavigableMap<Double, Double> map = weightByOption.get(n-1);
        Double maybe = map.get(budget);
        if(maybe != null) return maybe;
        double key = budget;
        Double before = map.floorKey(key);
        Double after = map.ceilingKey(key);
        if (before == null) return after;
        if (after == null) return before;
        return (key - before < after - key
                || after - key < 0)
                && key - before > 0 ? before : after;
    }
    void put(int n, double budget, double val) {
        weightByOption.get(n-1).put(budget, val);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int n = 1; n <= weightByOption.size(); n++) {
            sb.append("n = " + n + "\n");
            TreeMap<Double, Double> tm = new TreeMap<>();
            tm.putAll(weightByOption.get(n-1));
            for(Map.Entry<Double,Double> entry : tm.entrySet()) {
                sb.append(entry.getKey());
                sb.append(", ");
                sb.append(entry.getValue());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
