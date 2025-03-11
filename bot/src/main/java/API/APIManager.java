package API;
import Utils.Kline;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class APIManager {
    private final String apiKey;
    private final String secretKey;
    private static final String BASE_URL = "https://api.binance.com";

    public APIManager(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public String placeMarketOrder(String symbol, String side, double tradeAmount) {
        try {
            String endpoint = "/api/v3/order";
            String url = "https://api.binance.com" + endpoint;

            long timestamp = System.currentTimeMillis();
            String queryString = "symbol=" + symbol +
                    "&side=" + side +
                    "&type=MARKET" +
                    "&quantity=" + tradeAmount +
                    "&timestamp=" + timestamp;

            String signature = generateSignature(queryString);
            String postData = queryString + "&signature=" + signature;

            String response = sendSignedPostRequest(url, postData);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "Order failed: " + e.getMessage();
        }
    }

    public String sendSignedPostRequest(String urlString, String postData) {
        HttpURLConnection connection = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("X-MBX-APIKEY", apiKey);
            connection.setDoOutput(true);

            writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(postData);
            writer.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("HTTP error: " + responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            return null;
        } finally {
            try {
                if (writer != null) writer.close();
                if (reader != null) reader.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
            if (connection != null) connection.disconnect();
        }
    }


    public String sendGetRequest(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("HTTP error: " + responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return response.toString();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing reader: " + e.getMessage());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean testConnection() {
        String urlString = "https://api.binance.com/api/v3/ping";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                System.out.println("API responded with error: " + responseCode);
                return false;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Connection error: Request timed out");
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return false;
    }

    public String sendSignedGetRequest(String urlString) {
        try {
            long timestamp = System.currentTimeMillis();
            String query = "timestamp=" + timestamp;
            String signature = generateSignature(query);
            urlString += "?" + query + "&signature=" + signature;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-MBX-APIKEY", apiKey);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject getAllBalances() {
        String endpoint = "/api/v3/account";
        String url = BASE_URL + endpoint;

        try {
            String response = sendSignedGetRequest(url);
            JSONObject jsonResponse = new JSONObject(response);

            JSONObject balances = new JSONObject();
            for (Object obj : jsonResponse.getJSONArray("balances")) {
                JSONObject balance = (JSONObject) obj;
                String asset = balance.getString("asset");
                double free = balance.getDouble("free");
                if (free > 0) {
                    balances.put(asset, free);
                }
            }
            return balances;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateSignature(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public List<Kline> getKlines(String symbol, String interval, int limit) {
        JSONArray jsonArray = getKlinesData(symbol, interval, limit);
        if (jsonArray == null) return new ArrayList<>();

        List<Kline> klines = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray klineData = jsonArray.getJSONArray(i);
            long timestamp = klineData.getLong(0);
            double open = klineData.getDouble(1);
            double high = klineData.getDouble(2);
            double low = klineData.getDouble(3);
            double close = klineData.getDouble(4);
            double volume = klineData.getDouble(5);

            klines.add(new Kline(timestamp, open, high, low, close, volume));
        }
        return klines;
    }

    public JSONArray getKlinesData(String symbol, String interval, int limit) {
        String url = BASE_URL + "/api/v3/klines?symbol=" + symbol + "&interval=" + interval + "&limit=" + limit;
        String response = sendGetRequest(url);

        if (response == null || response.isEmpty()) {
            return null;
        }

        return new JSONArray(response);
    }

    //Some functions exampless
    public double getCurrentPrice(String symbol) {
        String url = BASE_URL + "/api/v3/ticker/price?symbol=" + symbol;
        String response = sendGetRequest(url);

        if (response == null || response.isEmpty()) {
            return -1;
        }

        JSONObject json = new JSONObject(response);
        return json.getDouble("price");
    }

    public double getAccountBalance(String asset) {
        try {
            String endpoint = "/api/v3/account";
            long timestamp = System.currentTimeMillis();
            String queryString = "timestamp=" + timestamp;
            String signature = generateSignature(queryString);

            String urlString = BASE_URL + endpoint + "?" + queryString + "&signature=" + signature;

            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-MBX-APIKEY", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("HTTP error: " + responseCode);
                return -1.0;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray balances = jsonResponse.getJSONArray("balances");

            for (int i = 0; i < balances.length(); i++) {
                JSONObject balanceObj = balances.getJSONObject(i);
                if (balanceObj.getString("asset").equals(asset)) {
                    return balanceObj.getDouble("free");
                }
            }

            return 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1.0;
        }
    }


    public JSONArray getOrderBook(String symbol, int limit) {
        String url = BASE_URL + "/api/v3/depth?symbol=" + symbol + "&limit=" + limit;
        String response = sendGetRequest(url);

        if (response == null || response.isEmpty()) {
            return null;
        }

        return new JSONObject(response).getJSONArray("bids");
    }

    public JSONArray getRecentTrades(String symbol, int limit) {
        String url = BASE_URL + "/api/v3/trades?symbol=" + symbol + "&limit=" + limit;
        String response = sendGetRequest(url);

        if (response == null || response.isEmpty()) {
            return null;
        }

        return new JSONArray(response);
    }

}
