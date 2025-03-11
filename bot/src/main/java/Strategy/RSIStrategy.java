package Strategy;

import API.APIManager;
import org.json.JSONArray;

public class RSIStrategy extends Strategy {

    public RSIStrategy(APIManager apiManager) {
        super(apiManager);
    }

    @Override
    public String execute(String symbol) {
        JSONArray klines = apiManager.getKlinesData(symbol, "5m", 15);

        if (klines == null || klines.length() < 15) {
            return "Not enough data";
        }

        double rsi = calculateRSI(klines, 14);
        if (rsi > 70) {
            return "SELL";
        } else if (rsi < 30) {
            return "BUY";
        }
        return "HOLD";
    }

    private double calculateRSI(JSONArray klines, int period) {
        double gain = 0, loss = 0;

        for (int i = 1; i <= period; i++) {
            double prevClose = klines.getJSONArray(i - 1).getDouble(4);
            double currClose = klines.getJSONArray(i).getDouble(4);
            double change = currClose - prevClose;

            if (change > 0) {
                gain += change;
            } else {
                loss += Math.abs(change);
            }
        }

        double avgGain = gain / period;
        double avgLoss = loss / period;

        if (avgLoss == 0) {
            return 100;
        }

        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }
}
