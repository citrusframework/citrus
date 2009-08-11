package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class Catch extends AbstractTestAction {
    /** List of actions to be executed */
    private List actions = new ArrayList();

    /** Exception type to be caught */
    private String exception = CitrusRuntimeException.class.getName();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Catch.class);

    /*
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        log.debug("Catch container catching exceptions of type " + exception);

        for (int i = 0; i < actions.size(); i++) {
            try {
                TestAction action = ((TestAction)actions.get(i));

                if (log.isDebugEnabled()) {
                    log.debug("Executing action " + action.getClass().getName());
                }

                action.execute(context);
            } catch (Exception e) {
                if (exception != null && exception.equals(e.getClass().getName())) {
                    log.info("Caught exception " + e.getClass() + ": " + e.getLocalizedMessage());
                    continue;
                }
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * @param actions
     */
    public void setActions(List actions) {
        this.actions = actions;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }
}
