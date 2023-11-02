package org.citrusframework.validation;

import java.util.Map;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public interface ValueMatcher {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    /** Message validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/value/matcher";

    /** Type resolver to find custom message validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, ValueMatcher> validators = new ConcurrentHashMap<>();

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, ValueMatcher> lookup() {
        if (validators.isEmpty()) {
            validators.putAll(TYPE_RESOLVER.resolveAll());

            if (logger.isDebugEnabled()) {
                validators.forEach((k, v) -> logger.debug(String.format("Found validator '%s' as %s", k, v.getClass())));
            }
        }
        return validators;
    }

    /**
     * Resolves validator from resource path lookup with given validator resource name. Scans classpath for validator meta information
     * with given name and returns instance of validator. Returns optional instead of throwing exception when no validator
     * could be found.
     * @param validator
     * @return
     */
    static Optional<ValueMatcher> lookup(String validator) {
        try {
            ValueMatcher instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve value matcher from resource '%s/%s'", RESOURCE_PATH, validator));
        }

        return Optional.empty();
    }

    /**
     * Filter supported value types
     * @param controlType
     * @return
     */
    boolean supports(Class<?> controlType);

    /**
     * Value matcher verifies the match of given received and control values.
     * @param received
     * @param control
     * @param context
     */
    boolean validate(Object received, Object control, TestContext context);
}
