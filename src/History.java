import java.util.ArrayList;
import java.util.List;

class History {
    private List<OptionHistory> historyByOption;
    public History(int K) {
        historyByOption = new ArrayList<>();
        for(int k = 0; k < K; k++) {
            historyByOption.add(new OptionHistory());
        }
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (OptionHistory oh : historyByOption) {
            sb.append(oh.toString());
        }
        return sb.toString();
    }
}
