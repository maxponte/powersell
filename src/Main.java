import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    static void testHistoryMulti() {
        int K = 7;
        History h = new History(K);
        List<Integer> lambdas = Arrays.asList(1,2,3,5,8,13,21);
        List<Integer> pis = Arrays.asList(1,5,3,15,7,17,12);
        List<Spread> s = new ArrayList<>();
        for(int i = 0; i < K; i++) {
            s.add(new Spread((double)pis.get(i), (double)lambdas.get(i)));
        }
        h.add(s);
        System.out.println("Hello World!" + h.getDayAhead(1, 1));
    }
    static List<List<Spread>> readPrices(int K) {
        String fileName = "/Users/ponte/Downloads/"+"da_rt_lmp_5021220.csv";
        // This will reference one line at a time
        String line = null;
        List<List<Spread>> results = new ArrayList<>();
        List<Spread> result = new ArrayList<>();

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            boolean first = true;
            double[] yesterdaysDayAhead = new double[K];
            int i = 0;
            while((line = bufferedReader.readLine()) != null) {
                if(first) {
                    first = false;
                    continue;
                }
                String[] s = line.split(",");
                result.add(new Spread(Double.parseDouble(s[4]), yesterdaysDayAhead[i]));
                if(result.size() == K) {
                    results.add(result);
                    result = new ArrayList<>();
                }
                yesterdaysDayAhead[i] = Double.parseDouble(s[8]);
                i = (i+1)%K;
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        return results;
    }
//    static void testHistorySingle() {
//        List<Spread> prices = readPrices();
//        int K = 1;
//        History h = new History(K);
//        h.addBulk(Collections.singletonList(prices));
//        double da = h.getDayAhead(1, 1);
//        double da2 = h.getDayAhead(2, 1);
//        if(da != 3.630000114) System.out.println("fail1");
//        if(da2 != 3.940000057) System.out.println("fail2");
//    }
    static void testTrader() {
        int K = 24;
        List<List<Spread>> prices = readPrices(K);
        int t = 0;
        double rho = 0.05;
        Trader trader = new Trader(K, rho, 1000, 100);
        for(List<Spread> s : prices) {
            if(t < 500) {
                trader.observe(s);
                t++;
                continue;
            }
            trader.trade(t, s);
            t++;
            if(t > 1000) break;
        }
    }
    public static void main(String[] args) {
//        testHistorySingle();
        testTrader();
    }
}
