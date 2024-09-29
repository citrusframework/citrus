package org.citrusframework.openapi.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.springframework.util.MultiValueMap;

public class OpenApiPayloadBuilder extends DefaultPayloadBuilder {

    public OpenApiPayloadBuilder(Object payload) {
        super(payload);
    }

    @Override
    public Object buildPayload(TestContext context) {

        if (getPayload() instanceof MultiValueMap<?,?> multiValueMap) {
            replaceDynamicContentInMultiValueMap(context,
                (MultiValueMap<Object, Object>) multiValueMap);
        }

        return super.buildPayload(context);
    }

    private static void replaceDynamicContentInMultiValueMap(TestContext context, MultiValueMap<Object, Object> multiValueMap) {
        Set<Object> cache = new HashSet<>(multiValueMap.entrySet());
        multiValueMap.clear();
        for (Object value : cache) {
            if (value instanceof Map.Entry<?, ?> entry) {
                replaceDynamicContentInEntry(context, multiValueMap, entry);
            }
        }
    }

    private static void replaceDynamicContentInEntry(TestContext context, MultiValueMap<Object, Object> multiValueMap,
        Entry<?, ?> entry) {
        Object key = entry.getKey();

        List<Object> list = (List<Object>) entry.getValue();
        if (list != null) {
            for (int i=0;i<list.size();i++) {
                Object listEntry = list.get(i);
                if (listEntry instanceof String text) {
                    list.set(i, context.replaceDynamicContentInString(text));
                }
            }
        }

        Object newKey = key instanceof String text ? context.replaceDynamicContentInString(text) : key;
        multiValueMap.put(newKey, list);
    }
}
