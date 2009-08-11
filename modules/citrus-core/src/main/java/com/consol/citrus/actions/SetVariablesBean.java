package com.consol.citrus.actions;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Bean to set variables during test workflow.
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class SetVariablesBean extends AbstractTestAction {

    /** Variables to set */
    private Map newVariables = new LinkedHashMap();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SetVariablesBean.class);

    /**
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute(TestContext)
     */
    @Override
    public void execute(TestContext context) {
        Iterator it = newVariables.keySet().iterator();
        String value;
        while (it.hasNext()) {
            String key = (String)it.next();
            value = (String)newVariables.get(key);

            log.info("Try to set variable: " +  key + " to value " + value);

            if (VariableUtils.isVariableName(value)) {
                value = context.getVariable(value);
            } else if(context.getFunctionRegistry().isFunction(value)) {
                value = FunctionUtils.resolveFunction(value, context);
            } 

            log.info("Setting variable: " + key + " to value: " + value);

            context.setVariable(key, value);
        }
    }

    /**
     * Setter for variables
     * @param variables
     */
    public void setVariables(Map variables) {
        this.newVariables = variables;
    }

}
