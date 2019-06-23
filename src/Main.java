import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    static void reformatLargeDataSet(String fileName, String outFileName) {
        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            FileWriter fw = new FileWriter(outFileName);
            BufferedWriter bw = new BufferedWriter(fw);
            SimpleDateFormat infmt = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
            SimpleDateFormat outfmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            bufferedReader.readLine(); // throw out 1st line
            while((line = bufferedReader.readLine()) != null) {
                String[] s = line.split(",", 10);
                Date d = infmt.parse(s[0]);
                String out = outfmt.format(d);
                String nodeID = s[2];
                String rt = s[4];
                String da = s[8];
                bw.write(out);
                bw.write(",");
                bw.write(nodeID);
                bw.write(",");
                bw.write(rt);
                bw.write(",");
                bw.write(da);
                bw.newLine();
            }
            bufferedReader.close();
            bw.close();
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
        }
        catch(ParseException pe) {
            System.out.println(
                    "Error parsing date '"
                            + line + "'");
        }
    }
    static List<List<Spread>> readPrices(int K, String fileName) {
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
                if(yesterdaysDayAhead[i] > 0.0) {
                    result.add(new Spread(Double.parseDouble(s[4]), yesterdaysDayAhead[i]));
//                    result.add(new Spread(yesterdaysDayAhead[i], Double.parseDouble(s[4])));
                    if (result.size() == K) {
                        results.add(result);
                        result = new ArrayList<>();
                    }
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
//    static void testTrader() {
//        int K = 24;
//        List<List<Spread>> prices = readPrices(K);
//        int t = 0;
//        double rho = 0.005;
//        Trader trader = new Trader(K, rho, 2000, 200);
//        for(List<Spread> s : prices) {
//            if(t < 290) {
//                trader.observe(s);
//                t++;
//                continue;
//            }
////            trader.debug();
////            break;
//            trader.trade(t, s);
//            t++;
//            if(t > 1000) break;
//        }
//    }
    static void testUCTrader(int K, double rho, int trainOffset, int testOffset, double startingBalance, int precision, String fileName) {
        List<List<Spread>> prices = readPrices(K, fileName);
        int t = 0;
        UnconstrainedTrader trader = new UnconstrainedTrader(K, rho, startingBalance, precision);
        int x = 0;
        for(List<Spread> s : prices) {
            x++;
            if(x < trainOffset) continue;
            if(t < testOffset) {
                trader.observe(s);
                t++;
                continue;
            }
//            System.out.println(trader.h);
//            trader.debug();
//            break;
            trader.trade(t, s);
            t++;
            if(t > 1000) break;
        }
    }
    public static void main(String[] args) {
        int K = Integer.parseInt(args[0]);
        double rho = Double.parseDouble(args[1]);
        int trainOffset = Integer.parseInt(args[2]);
        int testOffset = Integer.parseInt(args[3]);
        double startingBalance = Double.parseDouble(args[4]);
        int precision = Integer.parseInt(args[5]);
        String fileName = args[6];
//        testTrader();
//        String fileName = "/Users/ponte/Downloads/"+"cleaned_physical_nodes.csv";
//        String fileName = "/Users/ponte/Downloads/"+"da_rt_lmp_5021220.csv";
//        double rho = 0.002;
//        testUCTrader(24, rho, 100, 100, 200.0, 1, fileName);
        testUCTrader(K, rho, trainOffset, testOffset, startingBalance, precision, fileName);
//        testFileMap();
//        String fileName = "/Users/ponte/dpds_data/all.csv";
//        String outFileName = "/Users/ponte/dpds_data/fmt_all.csv";
//        reformatLargeDataSet(fileName, outFileName);
    }
}
