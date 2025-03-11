package Bot;

import API.APIManager;
import Manager.StrategyManager;
import Strategy.BollingerBandsStrategy;
import Strategy.MovingAverageStrategy;
import Strategy.RSIStrategy;
import Utils.Kline;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TradingBot {
    private final APIManager apiManager;
    private final StrategyManager strategyManager;
    private final String symbol;
    private MovingAverageStrategy movingAverageStrategy;
    private BollingerBandsStrategy bollingerBandsStrategy;
    private RSIStrategy rsiStrategy;
    private final double tradeAmount;

    public TradingBot(APIManager apiManager, String symbol, double tradeAmount) {
        this.apiManager = apiManager;
        this.strategyManager = new StrategyManager();
        this.symbol = symbol;
        this.tradeAmount = tradeAmount;
        this.movingAverageStrategy = new MovingAverageStrategy(this.apiManager);
        this.bollingerBandsStrategy = new BollingerBandsStrategy(this.apiManager);
        this.rsiStrategy = new RSIStrategy(this.apiManager);
    }

    public void startTrading() {
        while (true) {
            try {
                System.out.println("\nChecking market conditions...");
                List<Kline> klines = apiManager.getKlines(symbol, "5m", 50);
                String bestStrategy = strategyManager.getBestStrategy(klines);

                System.out.println("Market Condition: " + strategyManager.determineMarketType(strategyManager.calculateMarketIndicators(klines)));
                System.out.println("Selected Strategy: " + bestStrategy);



                String decision = executeStrategy(bestStrategy, klines,"BTCUSDT");
                if ("BUY".equals(decision)) {
                    placeMarketOrder("BUY");
                } else if ("SELL".equals(decision)) {
                    placeMarketOrder("SELL");
                } else {
                    System.out.println("No trade action taken (HOLD).");
                }

                System.out.println("Waiting 5 minutes before next check...");
                TimeUnit.MINUTES.sleep(5);
            } catch (Exception e) {
                System.err.println("Error during trading cycle: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String executeStrategy(String strategy, List<Kline> klines,String symbol) {
        switch (strategy) {
            case "Moving Average Strategy":
                return movingAverageStrategy.execute(symbol);
            case "Bollinger Bands Strategy":
                return bollingerBandsStrategy.execute(symbol);
            case "ATR Breakout Strategy":
                return rsiStrategy.execute(symbol);
            default:
                return "HOLD";
        }
    }

    private void placeMarketOrder(String side) {
        try {
            System.out.println("Placing " + side + " order for " + tradeAmount + " " + symbol);
            String result = apiManager.placeMarketOrder(symbol, side, tradeAmount);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Error placing order: " + e.getMessage());
        }
    }
}
