import org.decimal4j.util.DoubleRounder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Trader {
    History h;
    // V_n(b) is defined as the maximum payoff one can
    // collect in state b over the remaining n stage
    Payoff V;
    Weights w;
    BidService bidsvc;
    double[] bid;
    int step;
    double budget;
    int K;
    double rho;
    DoubleRounder dr;
    Trader(int K, double rho, int step, double budget) {
        this.h = new History(K);
        V = new Payoff(K);
        w = new Weights(K);
        bidsvc = new BidService(K);
        this.K = K;
        this.rho = rho;
        bid = new double[K];
        this.step = step;
        this.budget = budget;
        double inc = (double)step / budget;
        int precision = (int)Math.ceil(Math.log10(inc));
        dr = new DoubleRounder(precision);
        observe(Collections.singletonList(new Spread(0, 0)));
    }
    void observe(List<Spread> lampi) {
        // observe vectors (lam_t, pi_t)
        h.add(lampi);
//        System.out.println("observing");
//        System.out.println(Arrays.toString(lampi.toArray()));
    }
    void trade(int t, List<Spread> lampi) {
        // this is bid from yesterday's data
        bidsvc.bid(bid, lampi);

        Payoff rhat = new Payoff(K);
        observe(lampi);

        for(int n = 1; n <= K; n++) {
            int l = 2;
            int d = 0;
            int jp = step;

            /*
                Understanding what the statistics in OptionHistory provide:
                    For each historical DA price (lambda) in ascending order,
                        r = average daily profit if you always decide to buy when the price is at most lambda
                        v = average squared daily profit at lambda, basically variance of buying at lambda
                    Notably, there is:
                        No sense of price trends over time
                        No recursive sense of, hey, maybe I could decide to not ALWAYS buy up to the same maximum, regardless of what's going on
                Overview for K = 1 case:
                    For each possible budget $bdg < total budget,
                    find the maximum historical day ahead price we can still afford,
                    and estimate our payoff of spending as much of we can of this budget,
                    as the payoff of buying at that DA [amend. there is some weird moving average thing here]
                    Now, take the max payoff of spending at any budget $iBdg < $bdg, and bid $iBdg.
             */

            // optimize w
            for(int j = 1; j <= step; j++) {
                if(j % 10000 == 0) System.out.println("on step " + j);
                double bdg = (j*budget)/step;
//                if(bdg == budget) {
//                    System.out.println("full budget, d = "+ d);
//                }
                // what is this loop doing?
                // for a budget of $bdg = j*budget/step,
                // set rhat = the payoff associated with buying at the max historical DA price we can afford given $bdg
                // if we can afford the highest price for some j, stop running this for greater values of j
                while(d == 0) {
                    // k = K - n + 1
                    Double lam = h.getDayAhead(l, K-n+1);
                    if(lam > bdg) {
                        // the idea is, h.getDayAhead(l, K-n+1) is sorted in l,
                        // so if we can't afford l, we could afford l-1
                        // BUG: l = 2, we assume we can afford l-1...
                        double point = h.getMeanPayoff(l-1, K-n+1); // payoff associated with buying at l-1th highest DA price
                        double risk = (point*point)-h.getMeanSquarePayoff(l-1, K-n+1);
                        risk *= (t/t-1);
                        risk *= rho;
                        // we're saying, if you have $bdg, this is the payoff for only the step with n left
                        // so if n = 1, K = 1, then this is the total payoff
                        /*
                            Consider the scenario
                                budget = 20
                                dayAheads: [19.90999985, 20.5]
                                meanPay: [0.40999984500000153, -0.9499998099999978]
                                meanSqPay: [0.3361997458000506, 4.035397869000285]
                            We would want to buy at 19.9, for a payoff of 0.41
                            So rhat.put(1, 20, 0.41)
                         */
                        rhat.put(n, bdg, point + risk);
//                        if(bdg == 0.72) {
//                            System.out.println(h);
//                            System.out.println("0.72 budget, breaking, bdg = "+ bdg+ ", rhat = "+rhat.get(n, bdg));
//                            System.out.println("l"+ l);
//                        }
                        break;
                    } else if(l == t+1) {
                        // we can afford even the highest recorded day ahead price at this budget
                        // so we buy it, payoff is associated payoff
                        double point = h.getMeanPayoff(l, K-n+1);
                        double risk = (point*point)-h.getMeanSquarePayoff(l, K-n+1);
                        risk *= (t/t-1);
                        risk *= rho;
                        rhat.put(n, bdg, point + risk);
//                        System.out.println("setting d = 1 on j = " + j);
                        // stop looking for r hats at higher budgets (j > this j)
                        d = 1;
                        // jp allows for the optimization where we stop considering budgets even higher than this
                        jp = j;
                        break;
                    } else {
                        // we haven't found the max DA price we can afford within our budget
                        // so look at the next highest DA price
                        l++;
                    }
                }

                // now rhat tells us the best we could have done up to this pt
                // in this step we build out V_n from V_(n-1)
                double prev = V.get(n-1, bdg);
                // if you can convert to hash map before this section, helps a lot
                V.put(n, bdg, prev);
                w.put(n, bdg, 0);
                for(int i = 1; i <= Math.min(j, jp); i++) {
                    double iBdg = (i * budget) / step;
                    double vg = V.get(n - 1, ((j - i) * budget) / step);
                    double rg = rhat.get(n, iBdg);
                    double alt = vg + rg;
                    double existing = V.get(n, bdg);
//                    if(bdg == 0.72) {
//                        System.out.println("hm");
//                    }
                    if (existing <= alt) { // V_n(bdg) = max(V_n(bdg), alt)
                        V.put(n, bdg, alt);
                        w.put(n, bdg, iBdg);
                    }
                }
            }
//            System.out.println(w.toString());
        }

        // allocate w to bid vector
        double budgetRemaining = budget;
        bid = new double[K];
        for(int k = K; k >= 1; k--){
            bid[k-1] = w.get(k, budgetRemaining);
//            System.out.println("we think the payoff is " + V.get(k, budgetRemaining));
            budgetRemaining = dr.round(budgetRemaining - bid[k-1]);
//                System.out.println(V);
        }
//        System.out.println(h);
    }
}
