package com.wenruo.support;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Created by huangyao on 2017/4/7.
 */
public class Config {

    private static PropertiesConfiguration config = new PropertiesConfiguration();

    private static final String CONFIG_FILE = "/config.properties";

    static {
        try {
            String baseDir = Config.class.getResource("/").getFile();
            config.load(baseDir+CONFIG_FILE);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static String getString(String key) {
        return config.getString(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    public static int getInt(String key) {
        return config.getInt(key, 0);
    }

    public static long getLong(String key) {
        return config.getLong(key, 0l);
    }

    public static long getLong(String key, long defaultValue) {
        return config.getLong(key, defaultValue);
    }

    public static String[] getStringArray(String key) {
        return config.getStringArray(key);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return config.getBoolean(key, false);
    }

    public static float getFloat(String key, float defaultValue) {
        return config.getFloat(key, defaultValue);
    }

    public static float getFloat(String key) {
        return config.getFloat(key, 0);
    }
}
