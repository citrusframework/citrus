/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
