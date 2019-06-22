import java.util.Date;

public class Spread {
    double realTimePrice;
    double dayAheadPrice;
    int nodeID;
    Date date;
    int hour;
    boolean availableForPurchase = false;
    public Spread(double pi, double lambda) {
        realTimePrice = pi;
        dayAheadPrice = lambda;
    }
    public Spread(Date d, int nodeID, double pi, double lambda) {
        realTimePrice = pi;
        dayAheadPrice = lambda;
        this.nodeID = nodeID;
        date = d;
        hour = d.getHours();
    }

    @Override
    public String toString() {
        return "pi: " + realTimePrice + ", lambda: " + dayAheadPrice;
    }
}
