/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.variable.GlobalVariables;

/**
 * Factory bean implementation taking care of {@link FunctionRegistry} and {@link GlobalVariables}.
 * 
 * @author Christoph Deppisch
 */
public class TestContextFactoryBean implements FactoryBean {
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
    @Autowired
    private GlobalVariables globalVariables;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestContextFactoryBean.class);
    
    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
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

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
	@SuppressWarnings("unchecked")
    public Class getObjectType() {
        return TestContext.class;
    }

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
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
