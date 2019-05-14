import org.decimal4j.util.DoubleRounder;

import java.util.Arrays;
import java.util.List;

public class BidService {
    int[] holdings;
    double[] holdingPrices;
    int K;
    double earnings = 0.0;
    double leftoverBudgetMoney = 0.0;
    double costBasis = 0.0;
    int days = 0;
    DoubleRounder dr = new DoubleRounder(2);
    BidService(int K) {
        holdings = new int[K];
        holdingPrices = new double[K];
        this.K = K;
    }
    void bidMultis(List<Double> bids, List<Integer> bidOptions, List<Spread> lampi, double budget) {
        leftoverBudgetMoney += budget;
        // sell holdings
        for (int i = 0; i < holdings.length; i++) {
            if(holdings[i] > 0) {
                // sell
                double actualPayoff = (holdings[i] * lampi.get(i).realTimePrice) - holdingPrices[i];
//                System.out.println("we sold " + i + " and got paid "+ actualPayoff);
                earnings += actualPayoff;
            }
        }
        days++;
        System.out.println("on day: " + days);
        System.out.println("earnings: " + dr.round(earnings));
        System.out.println("cost: " + dr.round(costBasis));
        System.out.println("budget leftovers: " + dr.round(leftoverBudgetMoney));
//        System.out.println("next bid: " + Arrays.toString(bids));
        holdings = new int[K];
        holdingPrices = new double[K];
//        System.out.println("n bids " + bids.size());
        for (int i = 0; i < bids.size(); i++) {
            int kIdx = bidOptions.get(i) - 1;
            double ourBid = bids.get(i);
//            System.out.println("kIdx: " + kIdx + ", bidding " + ourBid);
            double assetPrice = lampi.get(kIdx).dayAheadPrice;
            if(ourBid >= assetPrice) {
//                System.out.println("we bought "+ i);
                holdings[kIdx]++;
                holdingPrices[kIdx] += assetPrice;
                leftoverBudgetMoney -= assetPrice;
                costBasis += assetPrice;
            }
        }
    }
    void bid(double[] bids, List<Spread> lampi, double budget) {
        leftoverBudgetMoney += budget;
        // sell holdings
        for (int i = 0; i < holdings.length; i++) {
            if(holdings[i] == 1) {
                // sell
                double actualPayoff = lampi.get(i).realTimePrice - holdingPrices[i];
//                System.out.println("we sold " + i + " and got paid "+ actualPayoff);
                earnings += actualPayoff;
            }
        }
        days++;
        System.out.println("on day: " + days);
        System.out.println("earnings: " + dr.round(earnings));
        System.out.println("cost: " + dr.round(costBasis));
        System.out.println("budget leftovers: " + dr.round(leftoverBudgetMoney));
        System.out.println("next bid: " + Arrays.toString(bids));
        holdings = new int[K];
        holdingPrices = new double[K];
        for (int i = 0; i < bids.length; i++) {
            double assetPrice = lampi.get(i).dayAheadPrice;
            double ourBid = bids[i];
            if(ourBid >= assetPrice) {
//                System.out.println("we bought "+ i);
                holdings[i] = 1;
                holdingPrices[i] = assetPrice;
                leftoverBudgetMoney -= assetPrice;
                costBasis += assetPrice;
            }
        }
    }
}
