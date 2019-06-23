import org.decimal4j.util.DoubleRounder;

import java.util.Arrays;
import java.util.List;

public class BidService {
    int[] holdings;
    double[] holdingPrices;
    int K;
    double earnings = 0.0;
    double accountBalance;
    int days = 0;
    DoubleRounder dr = new DoubleRounder(2);
    BidService(int K, double initalBalance) {
        holdings = new int[K];
        holdingPrices = new double[K];
        this.K = K;
        System.out.println("day,n_trades,expected_today_earnings,today_earnings,total_earnings,account_balance");
        accountBalance = initalBalance;
    }
    double bidMultis(List<BidPolicy> bids, List<Spread> lampi) {
        // buy on day N
        holdings = new int[K];
        holdingPrices = new double[K];
//        System.out.println("n bids " + bids.size());
        double todaysEarnings = 0.0;
        double todaysExpectedEarnings = 0.0;
        int nTrades = 0;
        for (int i = 0; i < bids.size(); i++) {
            BidPolicy bp = bids.get(i);
            int kIdx = bp.optionID-1;
            double assetPrice = lampi.get(kIdx).dayAheadPrice;
//            System.out.println("kIdx: " + kIdx + ", bidding " + ourBid + " against " + assetPrice);
            if(bp.dMaxBid >= assetPrice) {
//                System.out.println("we bought "+ i);
//                holdings[kIdx]++;
//                holdingPrices[kIdx] += assetPrice;
                double actualPayoff = lampi.get(bp.optionID-1).realTimePrice - assetPrice;
                todaysEarnings += actualPayoff;
                todaysExpectedEarnings += bp.expectedPayoff;
                nTrades++;
//                System.out.println("traded " + i + " paying " + actualPayoff + " while expecting " + bp.expectedPayoff);
            }
        }
//        // sell on day N+1
//        for (int i = 0; i < holdings.length; i++) {
//            if(holdings[i] > 0) {
//                // sell
//                double proceeds = holdings[i] * lampi.get(i).realTimePrice;
////                System.out.println("we sold " + i + " and got paid "+ actualPayoff);
//                accountBalance += proceeds;
//                todaysEarnings += proceeds - holdingPrices[i];
//            }
//        }
        accountBalance += todaysEarnings;
        earnings += todaysEarnings;
        days++;
        System.out.print(days);
        System.out.print(",");
        System.out.print(nTrades);
        System.out.print(",");
        System.out.print(dr.round(todaysExpectedEarnings));
        System.out.print(",");
        System.out.print(dr.round(todaysEarnings));
        System.out.print(",");
        System.out.print(dr.round(earnings));
        System.out.print(",");
        System.out.println(dr.round(accountBalance));
//        System.out.println("next bid: " + Arrays.toString(bids));
        return accountBalance;
    }
    void bid(double[] bids, List<Spread> lampi, double budget) {
        accountBalance += budget;
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
//        System.out.println("cost: " + dr.round(costBasis));
        System.out.println("account balance: " + dr.round(accountBalance));
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
                accountBalance -= assetPrice;
//                costBasis += assetPrice;
            }
        }
    }
}
