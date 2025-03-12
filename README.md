# Binance-Trading-Bot-Java
This is a personal project built for fun—a complete implementation and an example of how to use the **Binance API** in Java. It allows automated trading with multiple strategies and real-time market data.
‼️It is not 100% functional yet
---

## **🚀 Features**
✅ **Connects to Binance API** (fetches market data, places orders)  
✅ **Three Trading Strategies**:
- **Moving Average Strategy** (Trend following)
- **Bollinger Bands Strategy** (Volatility-based)
- **ATR Breakout Strategy** (Breakout trading)  
✅ **Market Analysis** (Determines market type dynamically)  
✅ **Automatic Order Execution** (Market buy/sell orders)  
✅ **Real-time Data Fetching**  

---

## **🛠️ Installation Guide**
### **1️⃣ Install Dependencies**
This project uses **Maven** for dependency management.  
Here’s the `pom.xml` configuration:

```xml
<dependencies>
    <!-- Binance API Client -->
    <dependency>
        <groupId>co.oril</groupId>
        <artifactId>binance-api-client-java</artifactId>
        <version>1.0.1</version>
    </dependency>

    <!-- JSON Parsing -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20210307</version>
    </dependency>

    <!-- JUnit Testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.9.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.9.1</version>
    </dependency>

    <!-- Technical Analysis (TA4J) -->
    <dependency>
        <groupId>org.ta4j</groupId>
        <artifactId>ta4j-core</artifactId>
        <version>0.14</version>
    </dependency>

    <!-- MySQL Connector (if using a database) -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

### **2️⃣ Configure API Keys**
1. Go to **Binance → API Management** and create an API Key.  
2. In the `APIManager` class, add:
   ```java
   APIManager apiManager = new APIManager("YOUR_API_KEY", "YOUR_SECRET_KEY");
   ```
---

## **📌 Next Steps**
✅ **Add risk management**  
✅ **Optimize strategy execution**  
✅ **Enhance logging and notifications**  

---

## **📜 Disclaimer**
⚠️ **Use this bot at your own risk!** Trading involves financial risk, and this project is purely for educational purposes. **Not financial advice.**  

---

## **📬 Contact**
Have questions? Give me a message on discord.
---

