package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.citrusframework.validation.context.SchemaValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SchemaValidator<T extends SchemaValidationContext> {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    /** Schema validator resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/message/schemaValidator";

    /** Type resolver to find custom schema validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves all available validators from resource path lookup. Scans classpath for validator meta information
     * and instantiates those validators.
     * @return
     */
    static Map<String, SchemaValidator<? extends SchemaValidationContext>> lookup() {
        Map<String, SchemaValidator<?>> validators = TYPE_RESOLVER.resolveAll("", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");

        if (logger.isDebugEnabled()) {
            validators.forEach((k, v) -> logger.debug(String.format("Found message validator '%s' as %s", k, v.getClass())));
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
    static Optional<SchemaValidator<? extends SchemaValidationContext>> lookup(String validator) {
        try {
            SchemaValidator<? extends ValidationMatcher> instance = TYPE_RESOLVER.resolve(validator);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve validator from resource '%s/%s'", RESOURCE_PATH, validator));
        }

        return Optional.empty();
    }
    /**
     * Validates the given message against schemas
     * @param message The message to be validated
     * @param context The test context of the current test execution
     * @param validationContext The context of the validation to be used for the validation
     * @return A report holding the results of the validation
     */
    void validate(Message message, TestContext context, T validationContext);

    /**
     *
     * @param messageType
     * @param message
     * @return true if the message/message type can be validated by this validator
     */
    boolean supportsMessageType(String messageType, Message message);
}
