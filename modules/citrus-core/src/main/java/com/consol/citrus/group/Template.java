package com.consol.citrus.group;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Template to perform a block of other actions in sequence
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class Template extends AbstractTestAction {

    /** Name of sequence */
    private String name;

    /** List of actions to be executed */
    private List actions = new ArrayList();

    private Map parameter = new LinkedHashMap();
    
    @Autowired
    private FunctionRegistry functionRegistry;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Template.class);

    /*
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) {
        log.info("Executing action template '" + name + "' - containing " + actions.size() + " actions");

        for (Iterator iterator = parameter.keySet().iterator(); iterator.hasNext();) {
            String param = (String) iterator.next();

            String paramValue = (String)parameter.get(param);

            if (VariableUtils.isVariableName(paramValue)) {
                paramValue = context.getVariable(paramValue);
            } else if(functionRegistry.isFunction(paramValue)) {
                paramValue = FunctionUtils.resolveFunction(paramValue, context);
            } 

            log.info("Setting parameter for template " + param + "=" + paramValue);

            context.setVariable(param, paramValue);
        }

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = ((TestAction)actions.get(i));

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        }

        log.info("Template was executed successfully");
    }

    /**
     * @param actions
     */
    public void setActions(List actions) {
        this.actions = actions;
    }

    /**
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Map parameter) {
        this.parameter = parameter;
    }
}
