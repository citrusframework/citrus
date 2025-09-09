package org.citrusframework.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import static java.lang.Boolean.parseBoolean;

public class EnvUtils {

    private EnvUtils() {
        // static access only
    }

    @Nonnull
    public static String transformPropertyToEnv(@Nonnull String property) {
        return property.replace(".", "_").toUpperCase();
    }

    public static boolean booleanPropertyOrDefault(@Nonnull SystemProvider systemProvider,
        @Nonnull String propertyName, @Nonnull String envName, boolean defaultValue) {
        String value = systemProvider
            .getProperty(propertyName)
            .orElseGet(()->
                systemProvider.getEnv(envName)
                    .orElse(null));
        return StringUtils.isNotEmpty(value) ? parseBoolean(value) : defaultValue;
    }

    @Nonnull
    public static <T extends Enum<T>> T enumPropertyOrDefault(@Nonnull SystemProvider systemProvider, @Nonnull Class<T> enumClass,
        @Nonnull String propertyName, @Nonnull String envName, @Nonnull T defaultValue) {
        return Enum.valueOf(enumClass, systemProvider
            .getProperty(propertyName)
            .orElseGet(() ->
                systemProvider.getEnv(envName)
                    .orElseGet(defaultValue::name)));
    }

    @Nullable
    public static String getConfigurationProperty(@Nonnull SystemProvider systemProvider, @Nonnull String propertyName) {
        return systemProvider
            .getProperty(propertyName)
            .orElse(systemProvider.getEnv(transformPropertyToEnv(propertyName)).orElse(null));
    }

    @Nullable
    public static String getConfigurationProperty(@Nonnull SystemProvider systemProvider, @Nonnull String propertyName, @Nonnull String defaultValue) {
        return systemProvider
            .getProperty(propertyName)
            .orElse(systemProvider.getEnv(transformPropertyToEnv(propertyName)).orElse(defaultValue));
    }
}
