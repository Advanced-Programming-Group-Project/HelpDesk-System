package jm.edu.utech.helpdesk.client.util;
// Member 1 (Front-End)

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// reads client.properties; a -D system property overrides the file
// e.g. mvn javafx:run -Dapi.baseUrl=http://192.168.0.5:8080/api
public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/client.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("Could not read client.properties, using defaults: " + e.getMessage());
        }
    }

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPS.getProperty(key, defaultValue));
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String apiBaseUrl() {
        return get("api.baseUrl", "http://localhost:8080/api");
    }
}
