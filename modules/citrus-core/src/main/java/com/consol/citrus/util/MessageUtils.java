package com.consol.citrus.util;

public class MessageUtils {
    public static boolean isSpringIntegrationHeaderEntry(String key) {
        return key.startsWith("springintegration_");
    }
}
