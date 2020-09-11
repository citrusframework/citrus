package com.consol.citrus.util;

/**
 * @author Christoph Deppisch
 */
public interface TypeConverter {

    /**
     * Converts target object to required type if necessary.
     *
     * @param target
     * @param type
     * @param <T>
     * @return
     */
    <T> T convertIfNecessary(Object target, Class<T> type);
}
