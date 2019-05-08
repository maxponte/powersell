import java.util.ArrayList;
import java.util.List;

class History {
    private List<OptionHistory> historyByOption;
    double rho;
    public History(int K, double rho) {
        historyByOption = new ArrayList<>();
        for(int k = 0; k < K; k++) {
            historyByOption.add(new OptionHistory());
        }
        this.rho = rho;
    }
    public void add(List<Spread> spreadByOption) {
        int i = 0;
        for(Spread s : spreadByOption) {
            historyByOption.get(i).add(s.dayAheadPrice, s.realTimePrice);
            i++;
        }
    }
    public void addBulk(List<List<Spread>> spreadsByOption) {
        int i = 0;
        for(List<Spread> ls : spreadsByOption) {
            for(Spread s : ls) {
                historyByOption.get(i).add(s.dayAheadPrice, s.realTimePrice);
            }
            i++;
        }
    }
    public double getDayAhead(int l, int k) {
        return historyByOption.get(k-1).getDayAhead(l);
    }
    public double getMeanPayoff(int l, int k) {
        return historyByOption.get(k-1).getMeanPayoff(l);
    }
    public double getMeanSquarePayoff(int l, int k) {
        return historyByOption.get(k-1).getMeanSquarePayoff(l);
    }
    public double getEstimatedPayoff(int t, int l, int k) {
        double point = getMeanPayoff(l, k); // payoff associated with buying at lth highest DA price
        double risk = (point*point)-getMeanSquarePayoff(l, k);
        risk *= (t/t-1);
        risk *= rho;
        return point + risk;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (OptionHistory oh : historyByOption) {
            sb.append(oh.toString());
        }
        return sb.toString();
    }
}
