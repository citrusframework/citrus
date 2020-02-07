package com.consol.citrus.json;

import java.util.Optional;

import com.consol.citrus.message.Message;

/**
 * @author Christoph Deppisch
 */
public final class JsonUtils {

    /**
     * Checks if message payload is of type Json. An empty payload is considered to be a valid Json payload.
     * @param message to check.
     * @return true if payload is Json, false otherwise.
     */
    public static boolean hasJsonPayload(Message message) {
        if (!(message.getPayload() instanceof String)) {
            return false;
        }

        return Optional.ofNullable(message.getPayload(String.class))
                .map(String::trim)
                .map(payload -> payload.length() == 0 || payload.startsWith("{") || payload.startsWith("["))
                .orElse(true);
    }
}
