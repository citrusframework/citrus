package com.consol.citrus;

/**
 * Context provider registered via resource path lookup. When module is on classpath this provider will be used to instantiate
 * Citrus.
 *
 * @author Christoph Deppisch
 */
public class CitrusSpringContextProvider implements CitrusContextProvider {

    @Override
    public CitrusContext create() {
        return CitrusSpringContext.create();
    }
}
