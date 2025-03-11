package Strategy;
import API.APIManager;
import Utils.Kline;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MovingAverageStrategy extends Strategy {

    public MovingAverageStrategy(APIManager apiManager) {
        super(apiManager);
    }

    @Override
    public String execute(String symbol) {
        JSONArray klinesArray = apiManager.getKlinesData(symbol, "5m", 50);

        if (klinesArray == null) {
            return "Error fetching Klines data.";
        }

        List<Kline> klines = convertJsonArrayToKlineList(klinesArray);

        if (klines.isEmpty()) {
            return "No valid Klines data.";
        }

        double movingAverage = calculateSMA(klines, 10);
        System.out.println("10-period SMA: " + movingAverage);

        double currentPrice = klines.get(klines.size() - 1).getClosePrice();
        if (currentPrice > movingAverage) {
            return "BUY";
        } else if (currentPrice < movingAverage) {
            return "SELL";
        }
        return "HOLD";
    }

    private List<Kline> convertJsonArrayToKlineList(JSONArray jsonArray) {
        List<Kline> klinesList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray klineData = jsonArray.getJSONArray(i);

            long openTime = klineData.getLong(0);
            double open = klineData.getDouble(1);
            double high = klineData.getDouble(2);
            double low = klineData.getDouble(3);
            double close = klineData.getDouble(4);
            double volume = klineData.getDouble(5);

            klinesList.add(new Kline(openTime, open, high, low, close, volume));
        }

        return klinesList;
    }

    private double calculateSMA(List<Kline> klines, int period) {
        if (klines.size() < period) {
            return 0.0;
        }

        double sum = 0;
        for (int i = klines.size() - period; i < klines.size(); i++) {
            sum += klines.get(i).getClose();
        }

        return sum / period;
    }
}
