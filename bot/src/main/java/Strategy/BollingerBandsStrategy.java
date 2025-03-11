package Strategy;

import API.APIManager;
import org.json.JSONArray;

public class BollingerBandsStrategy extends Strategy {

    public BollingerBandsStrategy(APIManager apiManager) {
        super(apiManager);
    }

    @Override
    public String execute(String symbol) {
        JSONArray klines = apiManager.getKlinesData(symbol, "5m", 20);

        if (klines == null || klines.length() < 20) {
            return "Not enough data";
        }

        double[] bollingerBands = calculateBollingerBands(klines, 20);
        double closePrice = klines.getJSONArray(klines.length() - 1).getDouble(4);

        if (closePrice > bollingerBands[1]) {
            return "SELL";  // Price above upper band
        } else if (closePrice < bollingerBands[0]) {
            return "BUY";   // Price below lower band
        }
        return "HOLD";      // Price within bands
    }

    private double[] calculateBollingerBands(JSONArray klines, int period) {
        double sum = 0;
        double[] closePrices = new double[period];

        for (int i = 0; i < period; i++) {
            closePrices[i] = klines.getJSONArray(i).getDouble(4);
            sum += closePrices[i];
        }

        double mean = sum / period;
        double variance = 0;
        for (double price : closePrices) {
            variance += Math.pow(price - mean, 2);
        }
        double stdDev = Math.sqrt(variance / period);

        double upperBand = mean + (2 * stdDev);
        double lowerBand = mean - (2 * stdDev);

        return new double[]{lowerBand, upperBand};
    }
}
