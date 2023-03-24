package org.citrusframework.functions;

/**
 * Default registry automatically adds default function library.
 * @author Christoph Deppisch
 */
public class DefaultFunctionRegistry extends FunctionRegistry {

    /**
     * Constructor initializes with default function library.
     */
    public DefaultFunctionRegistry() {
        this.addFunctionLibrary(new DefaultFunctionLibrary());
    }
}
