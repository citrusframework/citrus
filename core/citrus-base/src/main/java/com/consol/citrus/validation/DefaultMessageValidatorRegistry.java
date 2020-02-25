package com.consol.citrus.validation;

import java.io.IOException;
import java.util.stream.Stream;

import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Christoph Deppisch
 */
public class DefaultMessageValidatorRegistry extends MessageValidatorRegistry {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultMessageValidatorRegistry.class);

    /**
     * Default constructor adds message validator implementations from resource path lookup.
     */
    public DefaultMessageValidatorRegistry() {
        lookupValidators();
    }

    /**
     * Add message validators via resource path lookup.
     */
    private void lookupValidators() {
        try {
            TypeResolver typeResolver = new ResourcePathTypeResolver(RESOURCE_PATH);
            Stream.of(new PathMatchingResourcePatternResolver().getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + RESOURCE_PATH + "/*"))
                    .forEach(file -> {
                        String resourceName = file.getFilename();
                        MessageValidator<?> validator = typeResolver.resolve(resourceName);
                        String validatorName = typeResolver.resolveProperty(resourceName, "name");
                        log.info(String.format("Register message validator '%s' as %s", validatorName, validator.getClass()));
                        getMessageValidators().put(validatorName, validator);
                    });
        } catch (IOException e) {
            log.warn("Failed to resolve list of message validators - message validator registry might be empty", e);
        }
    }

}
