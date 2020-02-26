package com.consol.citrus.spi;

/**
 * @author Christoph Deppisch
 */
public interface ReferenceRegistry {

    void bind(String name, Object value);
}
