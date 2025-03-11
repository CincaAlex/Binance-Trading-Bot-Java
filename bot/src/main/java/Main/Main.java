package Main;

import API.APIManager;
import Bot.TradingBot;

public class Main {
    public static void main(String[] args) {
        APIManager apiManager = new APIManager("YOUR_API_KEY", "YOUR_SECRET_KEY");
        TradingBot bot = new TradingBot(apiManager, "BTCUSDT", 0.001);

        bot.startTrading();
    }
}

