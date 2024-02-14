import java.io.*;
import java.util.Properties;

public class ConfigReader {

    public static String getRootDirectory() throws IOException {
        String currentDirectory = System.getProperty("user.dir").replace("\\", "/");
        Properties config = new Properties();
        config.load(new FileInputStream("config.ini"));
        return config.getProperty("RootDirectory").replaceFirst("^~", currentDirectory);
    }

    public static int getPort(String configFilePath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFilePath));

        // Default to port 8080 if the property is not found
        return Integer.parseInt(properties.getProperty("Port", "8080"));
    }

    public static int getMaxThreads(String configFilePath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configFilePath));

        // Return number of threads
        return Integer.parseInt(properties.getProperty("MaxThreads", "10"));
    }
}
