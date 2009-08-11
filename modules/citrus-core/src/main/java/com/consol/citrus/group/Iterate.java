package com.consol.citrus.group;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.BooleanExpressionParser;

public class Iterate extends AbstractTestAction {
    /** List of actions to be executed */
    private List actions = new ArrayList();

    private String condition;

    private String indexName;

    private int index = 1;

    private int step = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Iterate.class);

    @Override
    public void execute(TestContext context) throws CitrusRuntimeException {
        log.info("Executing iterate loop - containing " + actions.size() + " actions");

        try {
            condition = context.replaceDynamicContentInString(condition);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        while (checkCondition()) {
            executeActions(context);

            index = index + step ;
        }
    }

    private void executeActions(TestContext context) throws CitrusRuntimeException {
        context.setVariable(indexName, Integer.valueOf(index).toString());

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = ((TestAction)actions.get(i));

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        }
    }

    private boolean checkCondition() throws CitrusRuntimeException {
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

    /**
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
