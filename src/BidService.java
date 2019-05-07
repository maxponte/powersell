import java.util.Arrays;
import java.util.List;

public class BidService {
    int[] holdings;
    double[] holdingBids;
    int K;
    double accountSize = 0.0;
    BidService(int K) {
        holdings = new int[K];
        holdingBids = new double[K];
        this.K = K;
    }
    void bid(double[] bids, List<Spread> lampi) {
        // sell holdings
        for (int i = 0; i < holdings.length; i++) {
            if(holdings[i] == 1) {
                // sell
                double actualPayoff = lampi.get(i).realTimePrice - holdingBids[i];
//                System.out.println("we sold " + i + " and got paid "+ actualPayoff);
                accountSize += actualPayoff;
                System.out.println("account balance: " + accountSize);
            }
        }
        System.out.println("bidding");
        System.out.println(Arrays.toString(bids));
        holdings = new int[K];
        holdingBids = new double[K];
        for (int i = 0; i < bids.length; i++) {
            double assetPrice = lampi.get(i).dayAheadPrice;
            double ourBid = bids[i];
            if(ourBid >= assetPrice) {
//                System.out.println("we bought "+ i);
                holdings[i] = 1;
                holdingBids[i] = ourBid;
            }
        }
    }
}
