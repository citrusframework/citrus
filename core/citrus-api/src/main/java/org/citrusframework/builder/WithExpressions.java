package org.citrusframework.builder;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public interface WithExpressions<B> {

    /**
     * Sets the expressions to evaluate. Keys are expressions that should be evaluated and values are target
     * variable names that are stored in the test context with the evaluated result as variable value.
     * @param expressions
     * @return
     */
    B expressions(Map<String, Object> expressions);

    /**
     * Add an expression that gets evaluated. The evaluation result is stored in the test context as variable with
     * given variable name.
     * @param expression
     * @param value
     * @return
     */
    B expression(final String expression, final Object value);
}
