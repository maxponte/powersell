import org.decimal4j.util.DoubleRounder;

import java.util.*;

public class BidService {
    Map<Integer, Integer> holdings;
    Map<Integer, Double> holdingCosts;
    int K;
    double earnings = 0.0;
    double leftoverBudgetMoney = 0.0;
    double costBasis = 0.0;
    int days = 0;
    DoubleRounder dr = new DoubleRounder(2);
    History history;
    BidService(int K, History h) {
        holdings = new HashMap<>();
        holdingCosts = new HashMap<>();
        history = h;
    }
    void bidMultis(Map<Integer, Double> bidPolicies, List<Spread> lampi, double budget) {
        // bidOptions gives you the absolute index of the option within History
        leftoverBudgetMoney += budget;
        // sell holdings
        for (int i = 0; i < lampi.size(); i++) {
            Spread s = lampi.get(i);
            int optionID = history.getOptionID(s.nodeID, s.hour);
            Integer holdingUnits = holdings.get(optionID);
            Double holdingCost = holdingCosts.get(optionID);
            if(holdingUnits != null) {
                // sell
                double actualPayoff = (holdingUnits * s.realTimePrice) - holdingCost;
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
        holdings = new HashMap<>();
        holdingCosts = new HashMap<>();
//        System.out.println("n bids " + bids.size());

        // buy
        for (int i = 0; i < lampi.size(); i++) {
            Spread s = lampi.get(i);
            int optionID = history.getOptionID(s.nodeID, s.hour);
            Double bidPolicy = bidPolicies.get(optionID);
            if(bidPolicy != null) {
                if(holdings.get(optionID) == null) {
                    holdings.put(optionID, 0);
                    holdingCosts.put(optionID, 0.0);
                }
                // why is this yesterday?
                double assetPrice = s.dayAheadPrice;
                if(bidPolicy >= assetPrice) {
                    holdings.put(optionID, holdings.get(optionID)+1);
                    holdingCosts.put(optionID, holdingCosts.get(optionID) + assetPrice);
                    leftoverBudgetMoney -= assetPrice;
                    costBasis += assetPrice;
                }
            }
        }
    }
    void bid(double[] bids, List<Spread> lampi, double budget) {
//        leftoverBudgetMoney += budget;
//        // sell holdings
//        for (int i = 0; i < holdings.length; i++) {
//            if(holdings[i] == 1) {
//                // sell
//                double actualPayoff = lampi.get(i).realTimePrice - holdingPrices[i];
////                System.out.println("we sold " + i + " and got paid "+ actualPayoff);
//                earnings += actualPayoff;
//            }
//        }
//        days++;
//        System.out.println("on day: " + days);
//        System.out.println("earnings: " + dr.round(earnings));
//        System.out.println("cost: " + dr.round(costBasis));
//        System.out.println("budget leftovers: " + dr.round(leftoverBudgetMoney));
//        System.out.println("next bid: " + Arrays.toString(bids));
//        holdings = new int[K];
//        holdingPrices = new double[K];
//        for (int i = 0; i < bids.length; i++) {
//            double assetPrice = lampi.get(i).dayAheadPrice;
//            double ourBid = bids[i];
//            if(ourBid >= assetPrice) {
////                System.out.println("we bought "+ i);
//                holdings[i] = 1;
//                holdingPrices[i] = assetPrice;
//                leftoverBudgetMoney -= assetPrice;
//                costBasis += assetPrice;
//            }
//        }
    }
}
