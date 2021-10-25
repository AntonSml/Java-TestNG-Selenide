package data;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.Properties;


public class DataUtils {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(DataUtils.class);
    private final Properties properties;

    public DataUtils() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("test-data.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return Integer.parseInt(properties.getProperty("id"));
    }

    public int getInvalidID() {
        return Integer.parseInt(properties.getProperty("invalid_id"));
    }

    public String getPhotoURL() {
        return properties.getProperty("photoURL");
    }

    public String getName() {
        return properties.getProperty("name");
    }

    public String getNewName() {
        return properties.getProperty("new_name");
    }

    public String getCategory() {
        return properties.getProperty("pet_category");
    }

    public String getTag() {
        return properties.getProperty("pet_tag");
    }


}
