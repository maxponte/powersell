import org.decimal4j.util.DoubleRounder;

import java.util.Collections;
import java.util.List;

public class Trader {
    History h; // statistics
    Payoff V; // max payoff for each budget x option
    Weights w; // how much to spend to achieve max payoff for each budget x option
    BidService bidsvc; // takes orders
    double[] bid; // allocation across K options
    int step; // # of discrete chunks budget is divided into
    double budget; // daily budget
    int K; // number of options. |nodes| * |hours in the day|
    DoubleRounder dr;
    Trader(int K, double rho, int step, double budget) {
        this.K = K;
        this.step = step;
        this.budget = budget;

        h = new History(K, rho);
        observe(Collections.singletonList(new Spread(0, 0)));

        V = new Payoff(K);
        w = new Weights(K);
        bidsvc = new BidService(K);
        bid = new double[K];

        double inc = (double)step / budget;
        int precision = (int)Math.ceil(Math.log10(inc));
        dr = new DoubleRounder(precision);
    }
    void observe(List<Spread> lampi) {
        h.add(lampi);
//        System.out.println("observing");
//        System.out.println(Arrays.toString(lampi.toArray()));
    }
    void trade(int t, List<Spread> lampi) {
        // this is the bid from yesterday's data
        bidsvc.bid(bid, lampi, budget);

        Payoff rhat = new Payoff(K);
        observe(lampi);

        // calculate estimated payoffs for each option, for all budgets
        for(int n = 1; n <= K; n++) {
            int l = 2;
            int d = 0;
            int jp = step;
            for(int j = 1; j <= step; j++) {
                double bdg = (j*budget)/step;
                // what's the payoff if we're willing to buy at the max affordable price within $bdg
                for(; d == 0; l++) {
                    // k = K - n + 1
                    Double lam = h.getDayAhead(l, K-n+1);
                    if(lam > bdg) {
                        rhat.put(n, bdg, h.getEstimatedPayoff(t, l-1, K-n+1));
                        break;
                    }
                    if(l == t+1) {
                        rhat.put(n, bdg, h.getEstimatedPayoff(t, l, K-n+1));
                        d = 1;
                        jp = j;
                        break;
                    }
                }

                // take the best payoff among all splits bdg = iBdg + rest
                double prev = V.get(n-1, bdg);
                V.put(n, bdg, prev);
                w.put(n, bdg, 0);
                for(int i = 1; i <= Math.min(j, jp); i++) {
                    double iBdg = (i * budget) / step;
                    double vg = V.get(n - 1, ((j - i) * budget) / step);
                    double rg = rhat.get(n, iBdg);
                    double alt = vg + rg;
                    double existing = V.get(n, bdg);
                    if (existing <= alt) { // V_n(bdg) = max(V_n(bdg), alt)
                        V.put(n, bdg, alt);
                        w.put(n, bdg, iBdg);
                    }
                }
            }
        }

        // allocate the overall budget across K options to maximize the payoff
        double budgetRemaining = budget;
        bid = new double[K];
        for(int k = K; k >= 1; k--){
            bid[k-1] = w.get(k, budgetRemaining);
            budgetRemaining = dr.round(budgetRemaining - bid[k-1]);
        }
    }
    public void debug() {
        System.out.println(h);
    }
}
