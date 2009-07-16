package com.consol.citrus;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

/**
 * Interface for all test action beans
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public interface TestAction {
    /**
     * Main execution method doing all work
     * @param context TODO
     * @throws TestSuiteException
     */
    public void execute(TestContext context) throws TestSuiteException;

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
