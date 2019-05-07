public class Spread {
    double realTimePrice;
    double dayAheadPrice;
    public Spread(double pi, double lambda) {
        realTimePrice = pi;
        dayAheadPrice = lambda;
    }

    @Override
    public String toString() {
        return "pi: " + realTimePrice + ", lambda: " + dayAheadPrice;
    }
}
