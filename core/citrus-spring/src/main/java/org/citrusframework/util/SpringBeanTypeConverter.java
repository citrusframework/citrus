package org.citrusframework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Christoph Deppisch
 */
public final class SpringBeanTypeConverter extends DefaultTypeConverter {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultTypeConverter.class);

    public static SpringBeanTypeConverter INSTANCE = new SpringBeanTypeConverter();

    /**
     * Private default constructor. Prevent instantiation users should use INSTANCE
     */
    private SpringBeanTypeConverter() {
    }

    @Override
    protected <T> Optional<T> convertBefore(Object target, Class<T> type) {
        if (Source.class.isAssignableFrom(type) &&
                target.getClass().isAssignableFrom(InputStreamSource.class)) {
            try {
                return (Optional<T>) Optional.of(new StreamSource(((InputStreamSource)target).getInputStream()));
            } catch (IOException e) {
                logger.warn("Failed to create stream source from object", e);
            }
        }

        if (target.getClass().isAssignableFrom(Resource.class)) {
            Resource resource = (Resource) target;
            if (File.class.isAssignableFrom(type)) {
                try {
                    return (Optional<T>) Optional.of(resource.getFile());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to access file from resource", e);
                }
            }

            if (InputStream.class.isAssignableFrom(type)) {
                try {
                    return (Optional<T>) Optional.of(resource.getInputStream());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to access input stream of resource", e);
                }
            }

            if (byte[].class.isAssignableFrom(type)) {
                try {
                    return (Optional<T>) Optional.of(FileUtils.copyToByteArray(resource.getInputStream()));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to access content of resource", e);
                }
            }
        }

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
                map.add(entry.getKey().toString(), arrayString.split(","));
            }

            return (Optional<T>) Optional.of(map);
        }

        return Optional.empty();
    }

    @Override
    public <T> T convertAfter(Object target, Class<T> type) {
        return new SimpleTypeConverter().convertIfNecessary(target, type);
    }
}
