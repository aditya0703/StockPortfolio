import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.*;

public class Portfolio {

    static DecimalFormat df = new DecimalFormat("#.##");
    static String filename = "Stocks.txt", marketPrice = "\"regularMarketPrice\":{\"raw\":", percentChange = "\"regularMarketChangePercent\":{\"raw\":";;
    static Map<String, Stock> portfolio = new LinkedHashMap<>();
    static int startingPortfolioTotal, currentPortfolioTotal;

    public static class Stock {
        String symbol;
        double startingInvestment,
                shares,
                startingPercent,
                startingPrice,
                currentPrice,
                dailyIncreasePercent,
                dailyPercentIncrease,
                currentTotal,
                currentIncrease,
                currentPercent,
                growth;

        Stock(String symbol, double startingInvestment, double shares) {

            this.symbol = symbol;
            this.startingInvestment = startingInvestment;
            this.shares = shares;
            this.startingPrice = Double.parseDouble(df.format(this.startingInvestment/this.shares));
        }

        void stockPrint() {
            System.out.println("[" + symbol
                    + ", Investment: $" + startingInvestment
                    + ", Shares: " + shares
                    + ", Starting Percent: " + startingPercent
                    + "%, Buy Price: $" + startingPrice
                    + ", Current Price: $" + currentPrice
                    + ", Current Total: $" + currentTotal
                    + ", Daily Change: " + dailyIncreasePercent
                    + "%, Increase: $" + currentIncrease
                    + ", Daily Increase Percent: " + dailyPercentIncrease
                    + "%, Current Percent: " + currentPercent
                    + "%, Growth: " + growth + "%]");
        }

    }

    public static void setPortfolio() {
        try {
            File file = new File(filename);
            FileReader reader = new FileReader(file);
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            while ((line=buffer.readLine()) != null ) {
                String[] result = line.trim().split("\\s*,\\s*");
                String symbol = result[0];
                double startingInvestment = Double.parseDouble(result[1]);
                double shares = Double.parseDouble(result[2]);
                Stock stock = new Stock(symbol, startingInvestment, shares);
                startingPortfolioTotal += startingInvestment;
                portfolio.put(symbol, stock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        portfolio.forEach( (k, v) -> {
            v.startingPercent = Double.parseDouble(df.format(100*v.startingInvestment/startingPortfolioTotal));
        });
    }

    public static double getUpdate(String symbol, String infoWanted) throws IOException {
        URL url = new URL("https://finance.yahoo.com/quote/" + symbol);
        URLConnection conn = url.openConnection();
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String line, info = infoWanted;
        int index;

        while ((line = br.readLine()) != null) {
            index = line.indexOf(infoWanted);
            if (index > - 1) {
                String stockInfo = line.substring(index);
                return Double.parseDouble(stockInfo.substring(infoWanted.length(), stockInfo.indexOf(",")));
            }
        }
        return 0;
    }

    public static void updatePortfolio() {
        portfolio.forEach( (k, v) -> { try {
            if (!k.equals("Cash")) {
                v.currentPrice = Double.parseDouble(df.format(getUpdate(k, marketPrice)));
                v.dailyIncreasePercent = Double.parseDouble(df.format(getUpdate(k, percentChange)));
            }
            else {
                v.currentPrice = v.startingPrice;
                v.dailyIncreasePercent = 0;
            }
        } catch (IOException e) { e.printStackTrace(); }

            v.dailyPercentIncrease = 100*v.currentPrice/v.startingPrice;
            v.currentTotal = v.currentPrice*v.shares;
            v.currentIncrease = v.currentTotal-v.startingInvestment;
            currentPortfolioTotal += v.currentTotal;
        });

        portfolio.forEach( (k, v) -> {
            v.currentPercent = Double.parseDouble(df.format(100*v.currentTotal/currentPortfolioTotal));
            v.growth = Double.parseDouble(df.format(v.currentPercent - v.startingPercent));
        });
    }

    public static void createCSV() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File("test.csv"));
        } catch (FileNotFoundException e) { System.out.println(e.getMessage()); }

        StringBuilder sb = new StringBuilder();
        String columns = "Name,"
                + "Investment,"
                + "Shares,"
                + "Starting Percent,"
                + "Starting Price,"
                + "Current Price,"
                + "Current Total,"
                + "Current Increase,"
                + "Daily Percent Increase,"
                + "Current Percent,"
                + "Percent Growth";

        sb.append(columns);
        portfolio.forEach( (k, v) -> {
            sb.append("\n" + v.symbol
                    + ",$" + v.startingInvestment
                    + "," + v.shares
                    + "," + v.startingPercent + "%"
                    + ",$" + v.startingPrice
                    + ",$" + v.currentPrice
                    + ",$" + v.currentTotal
                    + ",$" + v.currentIncrease
                    + "," + v.dailyIncreasePercent + "%"
                    + "," + v.currentPercent + "%"
                    + "," + v.growth + "%");
        });

        sb.append("\n\nTotal: ,$" + currentPortfolioTotal);
        writer.write(sb.toString());
        writer.close();
    }

    public static void main(String[] args) {
        setPortfolio();
        updatePortfolio();
        portfolio.forEach( (k, v) -> {v.stockPrint();});
        createCSV();
        System.out.println("Total: $" +  currentPortfolioTotal);
    }

}

// TODO Ideas
// Create a prompt to see if you sold any positions and you can update that .txt automatically by running the program
