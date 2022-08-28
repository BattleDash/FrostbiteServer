package me.battledash.kyber.util;

public class EnvUtil {

    public static <T> T getProperty(String env, String property, T defaultValue) {
        String value = System.getenv(env);
        if (value == null) {
            value = System.getProperty(property);
        }
        if (value == null) {
            if (defaultValue == null) {
                throw new IllegalArgumentException("No value found for property: " + property);
            }
            return defaultValue;
        }
        return (T) value;
    }

    public static <T> T getProperty(String env, String property) {
        return getProperty(env, property, null);
    }

}
