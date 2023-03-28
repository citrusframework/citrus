package org.citrusframework.util;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.GStringImpl;

/**
 * @author Christoph Deppisch
 */
public final class GroovyTypeConverter extends DefaultTypeConverter {

    public static GroovyTypeConverter INSTANCE = new GroovyTypeConverter();

    /**
     * Private default constructor. Prevent instantiation users should use INSTANCE
     */
    private GroovyTypeConverter() {
    }

    @Override
    protected <T> T convertBefore(Object target, Class<T> type) {
        if (GString.class.isAssignableFrom(type)) {
            return (T) new GStringImpl(new Object[]{ target }, new String[] {"", ""});
        } else if (GString.class.isAssignableFrom(target.getClass())) {
            return super.convertIfNecessary(((GString) target).toString(), type);
        }

        return null;
    }

    @Override
    public <T> T convertStringToType(String value, Class<T> type) {
        if (GString.class.isAssignableFrom(type)) {
            return (T) new GStringImpl(new Object[]{ value }, new String[] {"", ""});
        }

        return super.convertStringToType(value, type);
    }
}
