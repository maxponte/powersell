import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class OptionHistory {
    private List<Double> dayAheadPrices; // lambda
    private List<Double> meanPayoffs; // r
    private List<Double> meanSquarePayoffs; // v
    public OptionHistory() {
        dayAheadPrices = new ArrayList<>();
        meanPayoffs = new ArrayList<>();
        meanSquarePayoffs = new ArrayList<>();
    }
    // requires t > 0
    public void add(double dayAheadPrice, double realTimePrice) {
        double payoff = realTimePrice - dayAheadPrice;

        int t = dayAheadPrices.size() + 1;
        if(t == 1) {
            dayAheadPrices.add(dayAheadPrice);
            meanPayoffs.add(payoff);
            meanSquarePayoffs.add(payoff*payoff);
            return;
        }

        // i = i_k - 1
        int idx = 0;
        for(; idx < dayAheadPrices.size(); idx++) {
            if(dayAheadPrices.get(idx) >= dayAheadPrice) {
                break;
            }
        }
        idx--;

        // insert AFTER i_k
        // TODO this is actually slow. use a tree
        dayAheadPrices.add(idx+1, dayAheadPrice);

        double meanPayoffAtIdx = idx >= 0 ? meanPayoffs.get(idx) : 0;
        meanPayoffs.add(idx+1, meanPayoffAtIdx);

        double meanSquarePayoffAtIdx = idx >= 0 ? meanSquarePayoffs.get(idx) : 0;
        meanSquarePayoffs.add(idx+1, meanSquarePayoffAtIdx);

        for(int i = 0; i <= idx; i++) {
            meanPayoffs.set(i, meanPayoffs.get(i)*(t-1)/t);
            meanSquarePayoffs.set(i, meanSquarePayoffs.get(i)*(t-1)/t);
        }

        for(int i = idx+1; i < meanPayoffs.size(); i++) {
            meanPayoffs.set(i, (meanPayoffs.get(i)*(t-1)/t) + (payoff/t));
            meanSquarePayoffs.set(i, (meanSquarePayoffs.get(i)*(t-1)/t) + (payoff*payoff)/t);
        }
    }
    public double getDayAhead(int l) {
        return dayAheadPrices.get(l-1);
    }
    public double getMeanPayoff(int l) {
        return meanPayoffs.get(l-1);
    }
    public double getMeanSquarePayoff(int l) {
        return meanSquarePayoffs.get(l-1);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dayAheads: " + Arrays.toString(dayAheadPrices.toArray()));
        sb.append("\n");
        sb.append("meanPay: " + Arrays.toString(meanPayoffs.toArray()));
        sb.append("\n");
        sb.append("meanSqPay: " + Arrays.toString(meanSquarePayoffs.toArray()));
        sb.append("\n");
        return sb.toString();
    }
}
