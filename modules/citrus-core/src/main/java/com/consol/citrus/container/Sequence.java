package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

/**
 * Sequence to perform a block of other actions in sequence
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class Sequence extends AbstractTestAction {

    /** List of actions to be executed */
    private List actions = new ArrayList();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Sequence.class);

    /*
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        log.info("Executing action sequence - containing " + actions.size() + " actions");

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = ((TestAction)actions.get(i));

            if(log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }
            action.execute(context);
        }

        log.info("Action sequence finished successfully");
    }

    /**
     * @param actions
     */
    public void setActions(List actions) {
        this.actions = actions;
    }
}
