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
    // generates a list of days
    // each day is a list of spreads containing:
    // real time price on that day
    // corresponding day ahead price on previous day
    // nodeID
    static List<List<Spread>> readLargeDataset(String fileName) {
        List<List<Spread>> results = new ArrayList<>();
        List<Spread> result = new ArrayList<>();
        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            SimpleDateFormat datefmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            int cdom = -1;
            Map<Integer, double[]> daPricesYesterday = new HashMap<>();
            Map<Integer, double[]> daPricesToday = new HashMap<>();
            Map<Integer, Spread[]> spreadsYesterday = new HashMap<>();
            Map<Integer, Spread[]> spreadsToday = new HashMap<>();
            while((line = bufferedReader.readLine()) != null) {
                String[] s = line.split(",", 4);
                Date d = datefmt.parse(s[0]);
                int nodeID = Integer.parseInt(s[1]);
                if(daPricesToday.get(nodeID) == null) {
                    daPricesToday.put(nodeID, new double[24]);
                }
                if(daPricesYesterday.get(nodeID) == null) {
                    daPricesYesterday.put(nodeID, new double[24]);
                }
                if(spreadsYesterday.get(nodeID) == null) {
                    spreadsYesterday.put(nodeID, new Spread[24]);
                }
                if(spreadsToday.get(nodeID) == null) {
                    spreadsToday.put(nodeID, new Spread[24]);
                }
                double rt = Double.parseDouble(s[2]);
                double da = Double.parseDouble(s[3]);
                int dom = d.getDate();
                if(dom != cdom) {
                    // a new day
                    if(cdom != -1) {
                        results.add(result);
                        result = new ArrayList<>();
                    }
                    daPricesYesterday.put(nodeID, daPricesToday.get(nodeID));
                    daPricesToday.put(nodeID, new double[24]);
                    spreadsYesterday.put(nodeID, spreadsToday.get(nodeID));
                    spreadsToday.put(nodeID, new Spread[24]);
                    cdom = dom;
                }
                int hrs = d.getHours();
                daPricesToday.get(nodeID)[hrs] = da;
                double daPriceYesterday = daPricesYesterday.get(nodeID)[hrs];
                if(daPriceYesterday > 0.0) {
                    // there is a real time price today,
                    // look up matching k from yesterday,
                    // and set availableForPurchase flag
                    Spread spr = new Spread(d, nodeID, rt, daPriceYesterday);
                    spreadsToday.get(nodeID)[hrs] = spr;
                    Spread ySpr = spreadsYesterday.get(nodeID)[hrs];
                    if(ySpr != null) {
                        ySpr.availableForPurchase = true;
                    }
                    result.add(spr);
                }
            }
            results.add(result);
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
        }
        catch(ParseException pe) {
            System.out.println(
                    "Error parsing date '"
                            + line + "'");
        }
        return results;
    }
    // make a separate class that handles this
    // fmt: timestamp, id, realtime, dayahead
    // input: data with missing rows
    // output: (t2, id, realtime_t1, realtime_t2) where t2 = t1 + 1 day and data for t1 exists
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
                if(yesterdaysDayAhead[i] > 0.0) {
                    result.add(new Spread(Double.parseDouble(s[4]), yesterdaysDayAhead[i]));
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
    static void testUCGapTrader() {
        int K = 24;
        String fn = "/Users/ponte/dpds_data/test.csv";
        List<List<Spread>> prices = readLargeDataset(fn);
        int t = 0;
        double rho = 0.000;
        UnconstrainedGapTrader trader = new UnconstrainedGapTrader(K, rho, 2000, 200);
        for(List<Spread> s : prices) {
            if(t < 290) {
                trader.observe(s);
                t++;
                continue;
            }
//            trader.debug();
//            break;
            trader.trade(t, s, 200);
            t++;
            if(t > 1000) break;
        }
    }
    static void testFileMap() {
        String fileName = "/Users/ponte/dpds_data/all.csv";
        String outFileName = "/Users/ponte/dpds_data/fmt_all.csv";
        reformatLargeDataSet(fileName, outFileName);
    }
    static void testReadMappedFile() {
        String fn = "/Users/ponte/dpds_data/test.csv";
        readLargeDataset(fn);
    }
    public static void main(String[] args) {
//        testTrader();
//        testUCTrader();
//        testFileMap();
        testReadMappedFile();
    }
}
