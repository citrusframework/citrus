package com.consol.citrus.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * DelayBean to let the whole test suite sleep for an amount of time
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class DelayBean extends AbstractTestAction {
    /** Amount of time in seconds */
    private String delay;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(DelayBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        String value = null;

        if (VariableUtils.isVariableName(delay)) {
            value = context.getVariable(delay);
        } else if(context.getFunctionRegistry().isFunction(delay)) {
            value = FunctionUtils.resolveFunction(delay, context);
        } else {
            value = delay;
        }

        try {
            log.info("Sleeping " + value + " seconds");

            Thread.sleep((long)(new Double(value).doubleValue() * 1000));

            log.info("Returning after " + value + " seconds");
        } catch (InterruptedException e) {
            throw new TestSuiteException(e);
        }
    }

    /**
     * Setter for delay
     * @param delay
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }
}
