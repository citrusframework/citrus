package com.consol.citrus.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.variable.GlobalVariables;

public class TestContextFactoryBean implements FactoryBean {
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
    @Autowired
    private GlobalVariables globalVariables;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestContextFactoryBean.class);
    
    public Object getObject() throws Exception {
        TestContext context = new TestContext();
        context.setFunctionRegistry(functionRegistry);
        context.setGlobalVariables(globalVariables);
        
        if(log.isDebugEnabled()) {
            log.debug("TestContextFactory created test context '" + context
                    + "' using global variables: '"
                    + context.getGlobalVariables() + "'");
        }
        
        return context;
    }

    public Class getObjectType() {
        return TestContext.class;
    }

    public boolean isSingleton() {
        return false;
    }

    /**
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * @param globalVariables the globalVariables to set
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }

    /**
     * @return the globalVariables
     */
    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

}
