import java.util.*;

public class Payoff {
    List<NavigableMap<Double, Double>> atBudgetByOption;
    Payoff(int K) {
        atBudgetByOption = new ArrayList<>();
        for (int n = 0; n <= K; n++) {
            atBudgetByOption.add(new TreeMap<>());
        }
    }
    double get(int n, double budget) {
        // TODO factor w/ Weights
        if(n == 0) return 0.0;
        NavigableMap<Double, Double> map = atBudgetByOption.get(n);
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
        if(n == 0 && val != 0) {
            System.out.println("omg");
        }
        atBudgetByOption.get(n).put(budget, val);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int n = 1; n < atBudgetByOption.size(); n++) {
            sb.append("n = " + n + "\n");
            for(Map.Entry<Double,Double> entry : atBudgetByOption.get(n).entrySet()) {
                sb.append(entry.getKey());
                sb.append(", ");
                sb.append(entry.getValue());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
