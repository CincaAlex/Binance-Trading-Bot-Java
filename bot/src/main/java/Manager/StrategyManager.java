package Manager;

import Utils.Kline;
import Utils.MarketIndicators;

import java.util.List;

public class StrategyManager {
    private static final int ADX_PERIOD = 14;
    private static final int ATR_PERIOD = 14;
    private static final int BOLLINGER_PERIOD = 20;

    public MarketIndicators calculateMarketIndicators(List<Kline> klines) {
        int length = klines.size();
        if (length < BOLLINGER_PERIOD) {
            throw new IllegalArgumentException("Not enough data for calculations.");
        }

        double[] closePrices = new double[length];
        double[] highPrices = new double[length];
        double[] lowPrices = new double[length];
        double[] volumes = new double[length];

        for (int i = 0; i < length; i++) {
            closePrices[i] = klines.get(i).getClosePrice();
            highPrices[i] = klines.get(i).getHighPrice();
            lowPrices[i] = klines.get(i).getLowPrice();
            volumes[i] = klines.get(i).getVolume();
        }

        double adx = calculateADX(highPrices, lowPrices, closePrices);
        double atr = calculateATR(highPrices, lowPrices, closePrices);
        double bollingerWidth = calculateBollingerWidth(closePrices);
        double price = closePrices[length - 1];
        double prevClose = closePrices[length - 2];
        double volume = volumes[length - 1];
        double avgVolume = calculateAverage(volumes, 50);
        double upperBand = calculateBollingerUpper(closePrices);
        double lowerBand = calculateBollingerLower(closePrices);

        return new MarketIndicators(adx, atr, bollingerWidth, price, upperBand, lowerBand, prevClose, volume, avgVolume);
    }

    public String determineMarketType(MarketIndicators indicators) {
        if (indicators.adx > 25) return "Trending Market";
        if (indicators.adx < 20 && indicators.bollingerWidth < 0.02) return "Range-Bound Market";
        if (indicators.atr > 1.5 * indicators.avgVolume) return "High Volatility Market";
        if (indicators.atr < 0.5 * indicators.avgVolume && indicators.bollingerWidth < 0.015) return "Low Volatility Market";
        if (indicators.price > indicators.upperBand || indicators.price < indicators.lowerBand) return "Breakout Market";
        if ((indicators.prevClose > indicators.upperBand && indicators.price < indicators.upperBand) ||
                (indicators.prevClose < indicators.lowerBand && indicators.price > indicators.lowerBand)) return "Fake Breakout";
        if (indicators.volume > 2 * indicators.avgVolume) return "News-Driven Market";
        return "Unknown Market Type";
    }

    public String getBestStrategy(List<Kline> klines) {
        MarketIndicators indicators = calculateMarketIndicators(klines);
        String marketType = determineMarketType(indicators);
        return selectBestStrategy(marketType);
    }

    public String selectBestStrategy(String marketType) {
        switch (marketType) {
            case "Trending Market": return "Moving Average Strategy";
            case "Range-Bound Market": return "Bollinger Bands Strategy";
            case "High Volatility Market": return "ATR Breakout Strategy";
            case "Low Volatility Market": return "Wait for Breakout";
            case "Breakout Market": return "ATR Breakout Strategy";
            case "Fake Breakout": return "Avoid Trading / Use Confirmation";
            case "News-Driven Market": return "Trade Carefully / Use Volume Analysis";
            default: return "No Clear Strategy";
        }
    }

    private double calculateADX(double[] high, double[] low, double[] close) {
        int length = high.length;
        if (length < ADX_PERIOD) return 0;

        double[] tr = new double[length];
        double[] plusDM = new double[length];
        double[] minusDM = new double[length];

        for (int i = 1; i < length; i++) {
            tr[i] = Math.max(high[i] - low[i],
                    Math.max(Math.abs(high[i] - close[i - 1]), Math.abs(low[i] - close[i - 1])));
            plusDM[i] = (high[i] - high[i - 1] > low[i - 1] - low[i]) ? Math.max(high[i] - high[i - 1], 0) : 0;
            minusDM[i] = (low[i - 1] - low[i] > high[i] - high[i - 1]) ? Math.max(low[i - 1] - low[i], 0) : 0;
        }

        double smoothedTR = calculateAverage(tr, ADX_PERIOD);
        double smoothedPlusDM = calculateAverage(plusDM, ADX_PERIOD);
        double smoothedMinusDM = calculateAverage(minusDM, ADX_PERIOD);

        double plusDI = (smoothedPlusDM / smoothedTR) * 100;
        double minusDI = (smoothedMinusDM / smoothedTR) * 100;
        return Math.abs(plusDI - minusDI) / (plusDI + minusDI) * 100;
    }

    private double calculateATR(double[] high, double[] low, double[] close) {
        int length = high.length;
        if (length < ATR_PERIOD) return 0;

        double[] tr = new double[length];
        for (int i = 1; i < length; i++) {
            tr[i] = Math.max(high[i] - low[i],
                    Math.max(Math.abs(high[i] - close[i - 1]), Math.abs(low[i] - close[i - 1])));
        }
        return calculateAverage(tr, ATR_PERIOD);
    }

    private double calculateBollingerWidth(double[] close) {
        double upper = calculateBollingerUpper(close);
        double lower = calculateBollingerLower(close);
        return (upper - lower) / close[close.length - 1];
    }

    private double calculateBollingerUpper(double[] close) {
        double mean = calculateAverage(close, BOLLINGER_PERIOD);
        double stdDev = calculateStandardDeviation(close, BOLLINGER_PERIOD);
        return mean + (2 * stdDev);
    }

    private double calculateBollingerLower(double[] close) {
        double mean = calculateAverage(close, BOLLINGER_PERIOD);
        double stdDev = calculateStandardDeviation(close, BOLLINGER_PERIOD);
        return mean - (2 * stdDev);
    }

    private double calculateAverage(double[] data, int period) {
        int length = data.length;
        if (length < period) return 0;

        double sum = 0;
        for (int i = length - period; i < length; i++) {
            sum += data[i];
        }
        return sum / period;
    }

    private double calculateStandardDeviation(double[] data, int period) {
        int length = data.length;
        if (length < period) return 0;

        double mean = calculateAverage(data, period);
        double sum = 0;
        for (int i = length - period; i < length; i++) {
            sum += Math.pow(data[i] - mean, 2);
        }
        return Math.sqrt(sum / period);
    }
}
