package Utils;

public class MarketIndicators {
    public double adx, atr, bollingerWidth, price, upperBand, lowerBand, prevClose, volume, avgVolume;

    public MarketIndicators(double adx, double atr, double bollingerWidth, double price,
                            double upperBand, double lowerBand, double prevClose,
                            double volume, double avgVolume) {
        this.adx = adx;
        this.atr = atr;
        this.bollingerWidth = bollingerWidth;
        this.price = price;
        this.upperBand = upperBand;
        this.lowerBand = lowerBand;
        this.prevClose = prevClose;
        this.volume = volume;
        this.avgVolume = avgVolume;
    }
}

