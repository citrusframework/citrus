package org.citrusframework.validation;

/**
 * Default registry adds default message validators located via resource path lookup. This adds
 * all validators present on the classpath.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageValidatorRegistry extends MessageValidatorRegistry {

    /**
     * Default constructor adds message validator implementations from resource path lookup.
     */
    public DefaultMessageValidatorRegistry() {
        MessageValidator.lookup().forEach(this::addMessageValidator);
        SchemaValidator.lookup().forEach(this::addSchemaValidator);
    }

}
