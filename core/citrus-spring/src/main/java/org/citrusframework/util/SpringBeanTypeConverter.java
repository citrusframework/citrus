package org.citrusframework.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public final class SpringBeanTypeConverter extends DefaultTypeConverter {

    public static SpringBeanTypeConverter INSTANCE = new SpringBeanTypeConverter();

    /**
     * Private default constructor. Prevent instantiation users should use INSTANCE
     */
    private SpringBeanTypeConverter() {
    }

    @Override
    protected <T> T convertBefore(Object target, Class<T> type) {
        if (MultiValueMap.class.isAssignableFrom(type)) {
            String mapString = String.valueOf(target);

            Properties props = new Properties();
            try {
                props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replaceAll("\\]\\s*", "]\n")));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to reconstruct object of type map", e);
            }
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String arrayString = String.valueOf(entry.getValue()).replaceAll("^\\[", "").replaceAll("\\]$", "").replaceAll(",\\s", ",");
                map.add(entry.getKey().toString(), StringUtils.commaDelimitedListToStringArray(String.valueOf(arrayString)));
            }

            return (T) map;
        }

        return null;
    }

    @Override
    public <T> T convertAfter(Object target, Class<T> type) {
        return new SimpleTypeConverter().convertIfNecessary(target, type);
    }
}
