package Tests;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BinanceTestnetTrader {
    private static final String API_KEY = "YOUR_API_KEY";
    private static final String SECRET_KEY = "YOUR_SECRET_KEY";
    private static final String BASE_URL = "https://testnet.binance.vision";

    public static void main(String[] args) {
        BinanceTestnetTrader trader = new BinanceTestnetTrader();

        System.out.println("Order History: " + trader.getAllOrders("BTCUSDT"));

    }

    public String getAllOrders(String symbol) {
        try {
            long timestamp = getServerTime();
            String query = "symbol=" + symbol + "&timestamp=" + timestamp;
            String signature = hmacSHA256(query, SECRET_KEY);
            String urlStr = BASE_URL + "/api/v3/allOrders?" + query + "&signature=" + signature;

            return sendGetRequest(urlStr);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getOpenOrders(String symbol) {
        try {
            long timestamp = getServerTime();
            String query = "symbol=" + symbol + "&timestamp=" + timestamp;
            String signature = hmacSHA256(query, SECRET_KEY);
            String urlStr = BASE_URL + "/api/v3/openOrders?" + query + "&signature=" + signature;

            return sendGetRequest(urlStr);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String getAccountBalance() {
        try {
            long timestamp = getServerTime();
            String query = "timestamp=" + timestamp;
            String signature = hmacSHA256(query, SECRET_KEY);
            String urlStr = BASE_URL + "/api/v3/account?" + query + "&signature=" + signature;

            return sendGetRequest(urlStr);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String placeMarketOrder(String symbol, String side, double quantity) {
        try {
            long timestamp = getServerTime();
            String params = "symbol=" + symbol +
                    "&side=" + side +
                    "&type=MARKET" +
                    "&quantity=" + quantity +
                    "&timestamp=" + timestamp;

            String signature = hmacSHA256(params, SECRET_KEY);
            String urlStr = BASE_URL + "/api/v3/order?" + params + "&signature=" + signature;

            return sendPostRequest(urlStr);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String sendGetRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-MBX-APIKEY", API_KEY);

        return handleResponse(conn);
    }

    private String sendPostRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-MBX-APIKEY", API_KEY);
        conn.setDoOutput(true);

        return handleResponse(conn);
    }

    private String handleResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream()
        ));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return responseCode == 200 ? "Success: " + response : "Binance Error " + responseCode + ": " + response;
    }

    private long getServerTime() throws Exception {
        String urlStr = BASE_URL + "/api/v3/time";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.readLine();
        in.close();

        return Long.parseLong(response.replaceAll("\\D+", ""));
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);

        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
