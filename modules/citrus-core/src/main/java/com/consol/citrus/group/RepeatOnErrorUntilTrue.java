package com.consol.citrus.group;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.util.BooleanExpressionParser;

public class RepeatOnErrorUntilTrue extends AbstractTestAction {
    /** List of actions to be executed */
    private List actions = new ArrayList();

    private String condition;

    private String indexName;

    private int autoSleep = 1;

    private int index = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    @Override
    public void execute(TestContext context) throws TestSuiteException {
        log.info("Executing repeat-on-error loop - containing " + actions.size() + " actions");

        try {
            condition = context.replaceDynamicContentInString(condition);
        } catch (ParseException e) {
            throw new TestSuiteException(e);
        }

        do {
            try {
                executeActions(context);
                break;
            } catch (TestSuiteException e) {
                if (checkCondition()) {
                    throw new TestSuiteException(e);
                } else {
                    log.info("Caught exception of type " + e.getClass().getName() + " '" + e.getMessage() + "' - repeating because of error");
                }
            } finally {
                index++;
            }
        } while (!checkCondition());
    }

    private void executeActions(TestContext context) throws TestSuiteException {
        context.setVariable(indexName, Integer.valueOf(index).toString());

        if (autoSleep > 0) {
            log.info("Sleeping " + autoSleep + " seconds");

            try {
                Thread.sleep(autoSleep * 1000);
            } catch (InterruptedException e) {
                log.error("Error during doc generation", e);
            }

            log.info("Returning after " + autoSleep + " seconds");
        }

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = ((TestAction)actions.get(i));

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        }
    }

    private boolean checkCondition() throws TestSuiteException {
        String conditionString = condition;

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, Integer.valueOf(index).toString());
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }

    public void setActions(List actions) {
        this.actions = actions;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setAutoSleep(int autoSleep) {
        this.autoSleep = autoSleep;
    }
}
