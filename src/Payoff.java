import java.util.*;

public class Payoff {
    List<HashMap<Double, Double>> atBudgetByOption;
    Payoff(int K) {
        atBudgetByOption = new ArrayList<>();
        for (int n = 0; n <= K; n++) {
            atBudgetByOption.add(new HashMap<>());
        }
    }
    double get(int n, double budget) {
        if(n == 0 || budget == 0.0) return 0.0;
        HashMap<Double, Double> map = atBudgetByOption.get(n);
        Double r = map.get(budget);
        return r;
    }
    void put(int n, double budget, double val) {
        atBudgetByOption.get(n).put(budget, val);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int n = 1; n < atBudgetByOption.size(); n++) {
            sb.append("n = " + n + "\n");
            TreeMap<Double, Double> tm = new TreeMap<>();
            tm.putAll(atBudgetByOption.get(n));
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
