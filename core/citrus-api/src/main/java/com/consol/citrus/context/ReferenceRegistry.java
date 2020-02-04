package com.consol.citrus.context;

/**
 * @author Christoph Deppisch
 */
public interface ReferenceRegistry {

    void bind(String name, Object value);
}
