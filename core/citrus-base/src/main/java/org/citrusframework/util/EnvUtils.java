package org.citrusframework.util;

import static java.lang.Boolean.parseBoolean;

public class EnvUtils {

    private EnvUtils() {
        // static access only
    }

    public static String transformPropertyToEnv(String property) {
        return property.replace(".", "_").toUpperCase();
    }

    public static boolean booleanPropertyOrDefault(SystemProvider systemProvider,
        String propertyName, String envName, boolean defaultValue) {
        return parseBoolean(
            systemProvider
                .getProperty(propertyName)
                .orElseGet(()->
                    systemProvider.getEnv(envName)
                        .orElseGet(() -> Boolean.toString(defaultValue))));
    }

    public static <T extends Enum<T>> T enumPropertyOrDefault(SystemProvider systemProvider, Class<T> enumClass,
        String propertyName, String envName, T defaultValue) {
        return Enum.valueOf(enumClass, systemProvider
            .getProperty(propertyName)
            .orElseGet(() ->
                systemProvider.getEnv(envName)
                    .orElseGet(defaultValue::name)));
    }
}
