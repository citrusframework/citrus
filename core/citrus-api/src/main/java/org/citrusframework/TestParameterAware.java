package org.citrusframework;

import java.util.Map;

/**
 * Interface marks test case to support test parameters.
 * @author Christoph Deppisch
 */
public interface TestParameterAware {

    /**
     * Sets the parameters.
     * @param parameterNames the parameter names to set
     * @param parameterValues the parameters to set
     */
    void setParameters(final String[] parameterNames, final Object[] parameterValues);

    /**
     * Gets the test parameters.
     * @return the parameters
     */
    Map<String, Object> getParameters();
}
