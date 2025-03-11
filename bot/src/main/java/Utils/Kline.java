package Utils;

public class Kline {
    private final long timestamp;
    private final double openPrice;
    private final double highPrice;
    private final double lowPrice;
    private final double closePrice;
    private final double volume;

    public Kline(long timestamp, double openPrice, double highPrice, double lowPrice, double closePrice, double volume) {
        this.timestamp = timestamp;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getVolume() {
        return volume;
    }

    public double getClose() {
        return closePrice;
    }
}
