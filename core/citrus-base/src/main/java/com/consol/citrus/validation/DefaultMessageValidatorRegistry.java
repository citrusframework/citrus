package com.consol.citrus.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default registry adds default message validators located via resource path lookup. This adds
 * all validators present on the classpath.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageValidatorRegistry extends MessageValidatorRegistry {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultMessageValidatorRegistry.class);

    /**
     * Default constructor adds message validator implementations from resource path lookup.
     */
    public DefaultMessageValidatorRegistry() {
        MessageValidator.lookup().forEach((k, v) -> {
            addMessageValidator(k, v);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Register message validator '%s' as %s", k, v.getClass()));
            }
        });
    }

}
