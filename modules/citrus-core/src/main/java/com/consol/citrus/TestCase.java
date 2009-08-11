package com.consol.citrus;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Test case representation executing a list of TestActions in sequential order.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class TestCase implements BeanNameAware {

    /** Test chain containing test actions to be executed */
    private List testChain = new ArrayList();

    /** Further chain of test actions to be executed in any case (Success, error)
     * Usually used to clean up database in any case of test result */
    private List finallyChain = new ArrayList();

    /** Tests variables */
    private Map variableDefinitions = new HashMap();

    /** Variables valid for this test **/
    private TestContext context = new TestContext();

    /** Name of testcase */
    private String name = TestCase.class.getSimpleName();

    /** Meta-Info */
    private TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
    
    /** TestCase description */
    private String description;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestCase.class);

    /**
     * Method executes a test case.
     * @return boolean flag to mark success
     */
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing TestCase");
        }

        /* build up the global test variables in TestContext by
         * getting the names and the current values of all variables */
        Iterator itVariables = variableDefinitions.keySet().iterator();

        while (itVariables.hasNext()) {
            final String key = (String)itVariables.next();
            String value = (String)variableDefinitions.get(key);

            if (VariableUtils.isVariableName(value)) {
                value = context.getVariable(value);
            } else if(context.getFunctionRegistry().isFunction(value)) {
                value = FunctionUtils.resolveFunction(value, context);
            }

            context.setVariable(key, value);
        }

        /* Debug print all variables */
        if (context.hasVariables() && log.isDebugEnabled()) {
            log.debug("TestCase using the following global variables:");
            Iterator it = context.getVariables().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                log.debug(key + " = " + context.getVariables().get(key));
            }
        }

        /* execute the test actions */
        for (int i = 0; i < testChain.size(); i++) {
            final TestAction action = (TestAction)testChain.get(i);

            log.info("");
            log.info((i+1) + ". action in test chain");

            /* execute the test action and validate its success */
            action.execute(context);
        }
    }

    /**
     * Method that will be executed in any case of test case result (success, error)
     * Usually used to clean up the database in any case
     * @return boolean flag to mark success
     */
    public void finish() {
        if (!finallyChain.isEmpty()) {
            log.info("Now reaching finally block to finish test case");
        }

        /* walk through the finally chain and execute the actions in there */
        Iterator itActions = finallyChain.iterator();
        while (itActions.hasNext()) {
            TestAction action = (TestAction)itActions.next();

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            /* execute the test action and validate its success */
            action.execute(context);
        }
    }

    /**
     * Spring property setter.
     * @param testChain
     */
    public void setTestChain(List testChain) {
        this.testChain = testChain;
    }
    /**
     * Spring property setter.
     * @param variableDefinitions
     */
    public void setVariableDefinitions(Map variableDefinitions) {
        this.variableDefinitions = variableDefinitions;
    }

    /**
     * Get Actions count in this test case
     * @return count actions
     */
    public int getCountActions() {
        return testChain.size() + finallyChain.size();
    }

    /**
     * Setter for finally chain
     * @param finallyChain
     */
    public void setFinallyChain(List finallyChain) {
        this.finallyChain = finallyChain;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[testVariables:");

        for (Iterator iter = variableDefinitions.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            buf.append(key + "=" + variableDefinitions.get(key) + ";");
        }

        buf.append("] ");

        buf.append("[testChain:");

        for (Iterator iter = testChain.iterator(); iter.hasNext();) {
            String className = (String) iter.next().getClass().getName();
            buf.append(className + ";");
        }

        buf.append("] ");

        return super.toString() + buf.toString();
    }

    /**
     * Adding element to testChain.
     * @param testAction
     */
    public void addTestChainAction(TestAction testAction) {
        this.testChain.add(testAction);
    }

    /**
     * Adding element to finallyChain.
     * @param testAction
     */
    public void addFinallyChainAction(TestAction testAction) {
        this.finallyChain.add(testAction);
    }

    /**
     * @return the metaInfo
     */
    public TestCaseMetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * @param metaInfo the metaInfo to set
     */
    public void setMetaInfo(TestCaseMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the finallyChain
     */
    public List getFinallyChain() {
        return finallyChain;
    }

    /**
     * @return the testChain
     */
    public List getTestChain() {
        return testChain;
    }

    /**
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name) {
        if (this.name == null)
            this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the variables
     */
    public TestContext getTestContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setTestContext(TestContext context) {
        this.context = context;
    }
}
