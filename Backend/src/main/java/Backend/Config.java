package Backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            // In a real application, handle this more gracefully
            // For this example, we'll use default values if the file is not found
            System.out.println("Warning: config.properties not found. Using default database credentials.");
            properties.setProperty("db.url", "jdbc:mysql://localhost:3306/BusBooking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            properties.setProperty("db.user", "root");
            properties.setProperty("db.password", "sanjai");
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUser() {
        return properties.getProperty("db.user");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8081"));
    }
}
