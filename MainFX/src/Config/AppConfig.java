package Config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static Properties properties = new Properties();

    static {
        try {
            // تحميل الإعدادات من ملف
            properties.load(new FileInputStream("config.properties"));
        } catch (IOException e) {
            System.out.println("⚠️ Config file not found, using defaults");

            // إعدادات افتراضية
            properties.setProperty("database.url", "jdbc:mysql://localhost:3306/university_results_system");
            properties.setProperty("database.username", "root");
            properties.setProperty("database.password", "");
            properties.setProperty("app.name", "University Results System");
            properties.setProperty("app.version", "1.0.0");
            properties.setProperty("debug.mode", "false");
        }
    }

    public static String getDatabaseURL() {
        return properties.getProperty("database.url");
    }

    public static String getDatabaseUsername() {
        return properties.getProperty("database.username");
    }

    public static String getDatabasePassword() {
        return properties.getProperty("database.password");
    }

    public static String getAppName() {
        return properties.getProperty("app.name");
    }

    public static boolean isDebugMode() {
        return Boolean.parseBoolean(properties.getProperty("debug.mode"));
    }

    public static void printConfig() {
        System.out.println("📋 App Configuration:");
        System.out.println("=====================");
        System.out.println("App Name: " + getAppName());
        System.out.println("Version: " + properties.getProperty("app.version"));
        System.out.println("Database URL: " + getDatabaseURL());
        System.out.println("Database User: " + getDatabaseUsername());
        System.out.println("Debug Mode: " + isDebugMode());
        System.out.println("=====================");
    }
}