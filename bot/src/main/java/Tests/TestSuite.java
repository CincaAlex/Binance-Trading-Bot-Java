package Tests;

import API.APIManager;
import Utils.Kline;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TestSuite {
    public static void main(String[] args) {
        System.out.println("======= Running Tests =======");

        String apiKey = "YOUR_API_KEY";
        String secretKey = "YOUR_SECRET_KEY";
        APIManager apiManager = new APIManager(apiKey, secretKey);

        // Run tests
        //testAPIConnection(apiManager);
        //testAccountBalance(apiManager);
        //testBTC15MinKlines(apiManager);
        List<Kline> klines = new ArrayList<>();

        klines.add(new Kline(1, 100, 105, 98, 102, 1000));
        klines.add(new Kline(2, 102, 108, 100, 106, 1200));
        klines.add(new Kline(3, 106, 110, 104, 108, 1500));
        klines.add(new Kline(4, 108, 112, 107, 111, 1300));
        klines.add(new Kline(5, 111, 115, 109, 114, 1400));
        klines.add(new Kline(6, 114, 118, 112, 117, 1100));
        klines.add(new Kline(7, 117, 120, 115, 119, 1000));
        klines.add(new Kline(8, 119, 123, 117, 121, 1600));
        klines.add(new Kline(9, 121, 125, 120, 124, 1700));
        klines.add(new Kline(10, 124, 128, 122, 127, 1800));
        int shortMaPeriod = 3;
        int longMaPeriod = 5;
        int bollingerPeriod = 5;
        int atrPeriod = 5;

        calculateIndicators(klines);
    }

    public static void calculateIndicators(List<Kline> klines) {
        int period = 14;

        double[] adxValues = calculateADX(klines, period);
        double[] rsiValues = calculateRSI(klines, period);

        for (int i = 0; i < klines.size(); i++) {
            Kline kline = klines.get(i);
            double volume = kline.getVolume();

            System.out.println("Timestamp: " + kline.getTimestamp());
            System.out.println("ADX: " + adxValues[i]);
            System.out.println("RSI: " + rsiValues[i]);
            System.out.println("Volume: " + volume);
            System.out.println("-------------------------");
        }
    }

    private static double[] calculateADX(List<Kline> klines, int period) {
        int size = klines.size();
        double[] adx = new double[size];
        double[] plusDI = new double[size];
        double[] minusDI = new double[size];
        double[] tr = new double[size];
        double[] dx = new double[size];

        if (size < period * 2) {
            System.err.println("Not enough data for ADX calculation (need at least " + (period * 2) + " candles).");
            return adx;
        }

        for (int i = 1; i < size; i++) {
            double high = klines.get(i).getHighPrice();
            double low = klines.get(i).getLowPrice();
            double closePrev = klines.get(i - 1).getClosePrice();
            double highPrev = klines.get(i - 1).getHighPrice();
            double lowPrev = klines.get(i - 1).getLowPrice();

            double trueRange = Math.max(high - low, Math.max(Math.abs(high - closePrev), Math.abs(low - closePrev)));
            double plusDM = high - highPrev > lowPrev - low ? Math.max(high - highPrev, 0) : 0;
            double minusDM = lowPrev - low > high - highPrev ? Math.max(lowPrev - low, 0) : 0;

            tr[i] = trueRange;
            plusDI[i] = plusDM;
            minusDI[i] = minusDM;
        }

        double smoothedTR = 0, smoothedPlusDI = 0, smoothedMinusDI = 0;
        for (int i = 1; i < period; i++) {
            smoothedTR += tr[i];
            smoothedPlusDI += plusDI[i];
            smoothedMinusDI += minusDI[i];
        }

        for (int i = period; i < size; i++) {
            smoothedTR = (smoothedTR - smoothedTR / period) + tr[i];
            smoothedPlusDI = (smoothedPlusDI - smoothedPlusDI / period) + plusDI[i];
            smoothedMinusDI = (smoothedMinusDI - smoothedMinusDI / period) + minusDI[i];

            double plusDI14 = (smoothedPlusDI / smoothedTR) * 100;
            double minusDI14 = (smoothedMinusDI / smoothedTR) * 100;

            dx[i] = (Math.abs(plusDI14 - minusDI14) / (plusDI14 + minusDI14)) * 100;
        }

        double smoothedDX = 0;
        for (int i = period; i < Math.min(2 * period, size); i++) {
            smoothedDX += dx[i];
        }

        for (int i = 2 * period; i < size; i++) {
            smoothedDX = (smoothedDX - smoothedDX / period) + dx[i];
            adx[i] = smoothedDX / period;
        }

        return adx;
    }

    private static double[] calculateRSI(List<Kline> klines, int period) {
        int size = klines.size();
        double[] rsi = new double[size];

        if (size < period + 1) {
            System.err.println("Not enough data for RSI calculation (need at least " + (period + 1) + " candles).");
            return rsi;
        }

        double gain = 0, loss = 0;
        for (int i = 1; i <= period; i++) {
            double change = klines.get(i).getClosePrice() - klines.get(i - 1).getClosePrice();
            if (change > 0) gain += change;
            else loss -= change;
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;
        rsi[period] = 100 - (100 / (1 + avgGain / avgLoss));

        for (int i = period + 1; i < size; i++) {
            double change = klines.get(i).getClosePrice() - klines.get(i - 1).getClosePrice();
            gain = change > 0 ? change : 0;
            loss = change < 0 ? -change : 0;

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;
            rsi[i] = avgLoss == 0 ? 100 : 100 - (100 / (1 + avgGain / avgLoss));
        }

        return rsi;
    }


    public static double calculateSMA(List<Kline> klines, int index, int period) {
        if (index < period - 1) return 0;
        double sum = 0;
        for (int i = index - period + 1; i <= index; i++) {
            sum += klines.get(i).getClosePrice();
        }
        return sum / period;
    }

    public static double[] calculateBollingerBands(List<Kline> klines, int index, int period) {
        if (index < period - 1) return new double[]{0, 0, 0};

        double sma = calculateSMA(klines, index, period);
        double sumSq = 0;
        for (int i = index - period + 1; i <= index; i++) {
            double diff = klines.get(i).getClosePrice() - sma;
            sumSq += diff * diff;
        }
        double stdDev = Math.sqrt(sumSq / period);

        return new double[]{sma + 2 * stdDev, sma, sma - 2 * stdDev};
    }

    public static double calculateATR(List<Kline> klines, int index, int period) {
        if (index < period) return 0;
        double atrSum = 0;
        for (int i = index - period + 1; i <= index; i++) {
            double tr = calculateTrueRange(klines.get(i - 1), klines.get(i));
            atrSum += tr;
        }
        return atrSum / period;
    }

    public static double calculateTrueRange(Kline prevKline, Kline currentKline) {
        double highLow = currentKline.getHighPrice() - currentKline.getLowPrice();
        double highClose = Math.abs(currentKline.getHighPrice() - prevKline.getClosePrice());
        double lowClose = Math.abs(currentKline.getLowPrice() - prevKline.getClosePrice());
        return Math.max(highLow, Math.max(highClose, lowClose));
    }

    public static void testBTC15MinKlines(APIManager apiManager) {
        System.out.println("\n[TEST] Fetching BTC/USDT 15-min candlestick data...");
        String symbol = "BTCUSDT";
        String interval = "15m";  // 15-minute timeframe
        int limit = 10;  // Fetch last 10 candles

        try {
            JSONArray klines = apiManager.getKlinesData(symbol, interval, limit);
            if (klines == null || klines.isEmpty()) {
                System.out.println("Failed to fetch Klines data!");
                return;
            }

            System.out.println("Last 3 BTC/USDT candles (15-min):");
            for (int i = Math.max(0, klines.length() - 3); i < klines.length(); i++) {
                JSONArray candle = klines.getJSONArray(i);
                System.out.println("🕯️ " + formatCandleData(candle));
            }

        } catch (Exception e) {
            System.out.println("Error fetching BTC Klines: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String formatCandleData(JSONArray candle) {
        long timestamp = candle.getLong(0);
        double open = candle.getDouble(1);
        double high = candle.getDouble(2);
        double low = candle.getDouble(3);
        double close = candle.getDouble(4);
        double volume = candle.getDouble(5);

        return String.format("%s | Open: %.2f | High: %.2f | Low: %.2f | Close: %.2f | Volume: %.2f",
                new java.util.Date(timestamp), open, high, low, close, volume);
    }

    public static void testAPIConnection(APIManager apiManager) {
        System.out.println("\n[TEST] Checking API connection...");
        String symbol = "BTCUSDT";
        int limit = 10;

        try {
            if (apiManager.testConnection()) {
                System.out.println("API connection successful!");
            } else {
                System.out.println("API connection failed!");
            }
        } catch (Exception e) {
            System.out.println("API connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void testAccountBalance(APIManager apiManager) {
        System.out.println("\n[TEST] Checking account balance...");

        try {
            JSONObject balanceData = apiManager.getAllBalances();
            System.out.println("Account Balances:");
            System.out.println(balanceData.toString(4));

        } catch (Exception e) {
            System.out.println("Failed to fetch balances: " + e.getMessage());
            e.printStackTrace();
        }
    }
}