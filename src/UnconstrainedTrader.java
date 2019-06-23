import org.decimal4j.util.DoubleRounder;

import java.math.RoundingMode;
import java.util.*;

public class UnconstrainedTrader {
    History h; // statistics
    Payoff V; // max payoff for each budget x option
    BidService bidsvc; // takes orders

    // this stuff replaces "w" in Trader
    // list of max prices
    List<Double> bids;
    // corresponding list of options to buy
    List<Integer> bidOptions;

    double precision;
    int K; // number of options. |nodes| * |hours in the day|
    DoubleRounder dr;
    UnconstrainedTrader(int K, double rho, double startingBalance, int precision) {
        this.K = K;
        this.precision = precision;

        h = new History(K, rho);

        V = new Payoff(1); // unconstrained just needs a by budget payoff
        bidsvc = new BidService(K);
        bidsvc.accountBalance = startingBalance;
        bids = new ArrayList<>();
        bidOptions = new ArrayList<>();

        dr = new DoubleRounder(precision);
    }
//    void configureDR(double budget) {
////        double inc = (double)step / budget;
////        int precision = (int)Math.ceil(Math.log10(inc));
////        System.out.println("setting precision" + precision);
////        dr = new DoubleRounder(precision);
//        dr = new DoubleRounder(1);
//        budget = dr.round(budget, RoundingMode.DOWN);
////        double inc = (double)step / budget;
////        int precision = (int)Math.ceil(Math.log10(inc));
////        System.out.println("setting precision" + precision);
////        dr = new DoubleRounder(precision);
//    }
    void observe(List<Spread> lampi) {
        h.add(lampi);
//        System.out.println("observing");
//        System.out.println(Arrays.toString(lampi.toArray()));
    }

    // process:
    // on day 1, observe RT price today and DA price yesterday, come up with bids
    // on day 2+,
    void trade(int t, List<Spread> lampi) {
        // this is the bid from yesterday's data
        double doubleBudget = bidsvc.accountBalance;
        if(t > 1) {
            doubleBudget = bidsvc.bidMultis(bids, bidOptions, lampi);
        }
        double inflateFactor = Math.pow(10, 2-precision);
        int step = (int)dr.round(doubleBudget * inflateFactor, RoundingMode.DOWN);

        observe(lampi);

        V = new Payoff(1); // unconstrained just needs a by budget payoff
        // calculate estimated payoffs for each option, for all budgets

        // if we wanted to sell instead
        // we would need to do increasing estimates of tomorrow's real time price
        // and the associated payoff would just be DA
        // then the later allocator would work the same way, just with sell orders
        Payoff rhat = new Payoff(K);
        int[] jp = new int[K];
        for(int n = 1; n <= K; n++) {
            boolean d = false;
            jp[n-1] = step;
            for (int j = 1; j <= step; j++) {
                int bdg = j;
                for (int l = 2; !d; l++) {
                    // k = K - n + 1
                    Double lam = h.getDayAhead(l, K - n + 1);
                    if (lam*inflateFactor > bdg) {
                        double payoff = h.getEstimatedPayoff(t, l - 1, K - n + 1);
                        if(l > 1) rhat.put(n, bdg, payoff);
                        else rhat.put(n, bdg, 0);
                        break;
                    }
                    if (l == t + 1) {
                        rhat.put(n, bdg, h.getEstimatedPayoff(t, l, K - n + 1));
                        d = true;
                        jp[n-1] = j;
                        break;
                    }
                }
            }
        }

        HashMap<Integer, AbstractMap.SimpleEntry<Integer, Integer>> witness = new HashMap<>();
        for(int j = 1; j <= step; j++) {
//            double bdg = (j * budget) / step;
            int bdg = j;
            V.put(1, bdg, 0);
            for(int n = 1; n <= K; n++) {
                // take the best payoff among all splits bdg = iBdg + rest
                for(int i = 1; i <= Math.min(j, jp[n-1]); i++) {
                    int iBdg = i;
//                    double iBdg = (i * budget) / step;
//                    iBdg += piece;
//                    iBdg = dr.round(iBdg);
//                    double vg = V.get(1, ((j - i) * budget) / step);
                    double vg = V.get(1, bdg - iBdg);
                    double rg = rhat.get(n, iBdg);
                    double alt = vg + rg;
                    double existing = V.get(1, bdg);
                    if (existing < alt) { // V_n(bdg) = max(V_n(bdg), alt)
//                        System.out.println((K-n+1) + ", " + iBdg + ", " + rg);
                        V.put(1, bdg, alt);
//                        System.out.println("recommending " + (K-n+1) + " at " + iBdg + " for " + rg);
                        witness.put(bdg, new AbstractMap.SimpleEntry<>(iBdg, K-n+1));
//                        System.out.println("k");
                    }
                }
            }
        }

        bids = new ArrayList<>();
        bidOptions = new ArrayList<>();
        int budgetRemaining = step;
//        System.out.println(V);
        AbstractMap.SimpleEntry bidPolicy = witness.get(budgetRemaining);
        while(bidPolicy != null) {
            Integer bid = (Integer)bidPolicy.getKey();
            bids.add(((double)bid) / inflateFactor);
//            System.out.println("bidding at " + );
            bidOptions.add((Integer)bidPolicy.getValue());
            budgetRemaining = budgetRemaining - bid;
            bidPolicy = witness.get(budgetRemaining);
        }
    }
    public void debug() {
        System.out.println(h);
    }
}
