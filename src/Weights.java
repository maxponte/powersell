import java.util.*;

public class Weights {
    List<Map<Double, Double>> weightByOption;
    Weights(int K) {
        weightByOption = new ArrayList<>();
        for (int n = 1; n <= K; n++) {
            weightByOption.add(new HashMap<>());
        }
    }
    Double get(int n, double budget) {
        return weightByOption.get(n-1).get(budget);
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
