package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class Assert extends AbstractTestAction {
    /** TestAction to be executed */
    private TestAction action;

    /** Aserted exception */
    private String exception = CitrusRuntimeException.class.getName();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Assert.class);

    /*
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        log.info("Assert container asserting exceptions of type " + exception);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        } catch (Exception e) {
            log.info("Validating caught exception ...");
            if (exception != null) {
                if (exception.equals(e.getClass().getName())) {
                    log.info("Exception is " + e.getClass() + ": " + e.getLocalizedMessage());
                    log.info("Exception validation OK");
                    return;
                } else {
                    throw new CitrusRuntimeException("Caught exception does not fit expected exception type '" + exception + "' caught: " + e.getClass().getName());
                }
            }
        }

        throw new CitrusRuntimeException("Asserted exception " + exception + " was not thrown as expected");
    }

    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
    }
}
