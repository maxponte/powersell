import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class History {
    private List<OptionHistory> historyByOption;
    double rho;
    Map<Integer, int[]> nodeIDPositions; // maps nodeID => for each hour in the day, the index of the OptionHistory associated with this node, at this time of day
    public History(int K, double rho) {
        nodeIDPositions = new HashMap<>();
        historyByOption = new ArrayList<>();
        for(int k = 0; k < K; k++) {
            historyByOption.add(new OptionHistory());
        }
        this.rho = rho;
    }
    public int getOptionID(int nodeID, int hour) {
        return nodeIDPositions.get(nodeID)[hour]-1;
    }
    // returns a list of option indices into History corresponding to tradeable options
    // meaning the real time price tomorrow exists aka "availableForPurchase"
    public List<Integer> add(List<Spread> spreadByOption) {
        List<Integer> mapping = new ArrayList<>();
        for(Spread s : spreadByOption) {
            int idx;
            int[] posMap = nodeIDPositions.get(s.nodeID);
            if(posMap != null && posMap[s.hour] > 0) {
                idx = posMap[s.hour]-1;
            } else {
                if(posMap == null) {
                    nodeIDPositions.put(s.nodeID, new int[24]); // getting super messy...
                    // you need to make the distinction between all these pieces clear
                    // that we're mapping (nodeID, hour in day) => integer IDs
                }
                idx = historyByOption.size();
                historyByOption.add(new OptionHistory());
                nodeIDPositions.get(s.nodeID)[s.hour] = idx+1;
            }
            historyByOption.get(idx).add(s.dayAheadPrice, s.realTimePrice);
            if(s.availableForPurchase) mapping.add(idx);
        }
        return mapping;
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
