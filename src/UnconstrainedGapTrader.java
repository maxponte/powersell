import org.decimal4j.util.DoubleRounder;

import java.util.*;

public class UnconstrainedGapTrader {
    History h; // statistics
    Payoff V; // max payoff for each budget x option
    BidService bidsvc; // takes orders

    // this stuff replaces "w" in Trader
    // list of max prices
    List<Double> bids;
    // corresponding list of options to buy
    List<Integer> bidOptions;

    int step; // # of discrete chunks budget is divided into
    double budget; // daily budget
    int K; // number of options. |nodes| * |hours in the day|
    DoubleRounder dr;
    UnconstrainedGapTrader(int K, double rho, int step, double budget) {
        this.K = K;
        this.step = step;
        this.budget = budget;

        h = new History(K, rho);

        V = new Payoff(1); // unconstrained just needs a by budget payoff
        bidsvc = new BidService(K);
        bids = new ArrayList<>();
        bidOptions = new ArrayList<>();

        double inc = (double)step / budget;
        int precision = (int)Math.ceil(Math.log10(inc));
        dr = new DoubleRounder(precision);
    }
//    void observe(List<Spread> lampi) {
//        h.add(lampi);
////        System.out.println("observing");
////        System.out.println(Arrays.toString(lampi.toArray()));
//    }
    void trade(int t, List<Spread> lampi, double budget) {
        // this is the bid from yesterday's data
        if(t > 1) bidsvc.bidMultis(bids, bidOptions, lampi, budget);

        // gives us a mapping from this local k in K, to the absolute position in OptionHistory
        List<Integer> kMap = h.add(lampi);

        // calculate estimated payoffs for each option, for all budgets
        Payoff rhat = new Payoff(K);
        int[] jp = new int[K];
        for(int n = 1; n <= K; n++) {
            boolean d = false;
            jp[n-1] = step;
            for (int j = 1; j <= step; j++) {
                double bdg = (j * budget) / step;
                // what's the payoff if we're willing to buy at the max affordable price within $bdg
//                if(j == 1) {
//                    System.out.println(h);
//                    System.out.println("omg");
//                }
                for (int l = 2; !d; l++) {
                    // k = K - n + 1
                    Double lam = h.getDayAhead(l, kMap.get(K - n + 1));
                    if (lam > bdg) {
                        double payoff = h.getEstimatedPayoff(t, l - 1, kMap.get(K - n + 1));
                        if(l > 1) rhat.put(n, bdg, payoff);
                        else rhat.put(n, bdg, 0);
                        break;
                    }
                    if (l == t + 1) {
                        rhat.put(n, bdg, h.getEstimatedPayoff(t, l, kMap.get(K - n + 1)));
                        d = true;
                        jp[n-1] = j;
                        break;
                    }
                }
            }
        }

        HashMap<Double, AbstractMap.SimpleEntry<Double, Integer>> witness = new HashMap<>();
        for(int j = 1; j <= step; j++) {
            double bdg = (j * budget) / step;
            V.put(1, bdg, 0);
            for(int n = 1; n <= K; n++) {
                // take the best payoff among all splits bdg = iBdg + rest
                for(int i = 1; i <= Math.min(j, jp[n-1]); i++) {
                    double iBdg = (i * budget) / step;
                    double vg = V.get(1, ((j - i) * budget) / step);
                    double rg = rhat.get(n, iBdg);
                    double alt = vg + rg;
                    double existing = V.get(1, bdg);
                    if (existing < alt) { // V_n(bdg) = max(V_n(bdg), alt)
//                        System.out.println((K-n+1) + ", " + iBdg + ", " + rg);
                        V.put(1, bdg, alt);
                        witness.put(bdg, new AbstractMap.SimpleEntry<>(iBdg, kMap.get(K-n+1)));
                    }
                }
            }
        }

        bids = new ArrayList<>();
        bidOptions = new ArrayList<>();
        double budgetRemaining = budget;
        AbstractMap.SimpleEntry bidPolicy = witness.get(budgetRemaining);
        while(bidPolicy != null) {
            Double bid = (Double)bidPolicy.getKey();
            bids.add(bid);
            bidOptions.add((Integer)bidPolicy.getValue());
            budgetRemaining = dr.round(budgetRemaining - bid);
            bidPolicy = witness.get(budgetRemaining);
        }
    }
    public void debug() {
        System.out.println(h);
    }
}
