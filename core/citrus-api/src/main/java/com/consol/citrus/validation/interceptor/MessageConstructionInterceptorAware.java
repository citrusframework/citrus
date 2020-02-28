package com.consol.citrus.validation.interceptor;

/**
 * @author Christoph Deppisch
 */
public interface MessageConstructionInterceptorAware {

    /**
     * Adds a new message construction interceptor.
     * @param interceptor
     */
    void addMessageConstructionInterceptor(MessageConstructionInterceptor interceptor);
}
