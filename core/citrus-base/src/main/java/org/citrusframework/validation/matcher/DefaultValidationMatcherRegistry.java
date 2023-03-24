package org.citrusframework.validation.matcher;

/**
 * Default registry automatically adds default validation matcher library.
 * @author Christoph Deppisch
 */
public class DefaultValidationMatcherRegistry extends ValidationMatcherRegistry {

    /**
     * Constructor initializes with default validation matcher library.
     */
    public DefaultValidationMatcherRegistry() {
        this.addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());
    }
}
