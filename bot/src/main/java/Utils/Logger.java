package Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "trading_log.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = "[" + timestamp + "] " + message;

        System.out.println(logMessage);

        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(logMessage + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
