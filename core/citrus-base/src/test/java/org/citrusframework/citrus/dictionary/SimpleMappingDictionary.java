package org.citrusframework.citrus.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.variable.dictionary.AbstractDataDictionary;

/**
 * @author Christoph Deppisch
 */
public class SimpleMappingDictionary extends AbstractDataDictionary<String> {

    private final Map<String, String> mappings;

    public SimpleMappingDictionary() {
        this(new HashMap<>());
    }

    public SimpleMappingDictionary(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    protected void processMessage(Message message, TestContext context) {
        String payload = message.getPayload(String.class);

        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            payload = payload.replaceAll(mapping.getKey(), mapping.getValue());
        }

        message.setPayload(payload);
    }

    @Override
    public <R> R translate(String key, R value, TestContext context) {
        return value;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return true;
    }
}
