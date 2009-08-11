package com.consol.citrus;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Interface for all test action beans
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public interface TestAction {
    /**
     * Main execution method doing all work
     * @param context TODO
     */
    public void execute(TestContext context);

    /**
     * Name of TestAction injected as Spring bean name
     * @return name as String
     */
    public String getName();

    /**
     * Name of TestAction injected as Spring bean name
     * @return name as String
     */
    public void setName(String name);

    /**
     * Description of TestAction
     * @return description as String
     */
    public String getDescription();
}
