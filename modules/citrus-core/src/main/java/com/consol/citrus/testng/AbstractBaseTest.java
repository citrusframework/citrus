/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.testng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.variable.GlobalVariables;

@ContextConfiguration(locations = {"classpath:com/consol/citrus/spring/root-application-ctx.xml",
                                   "classpath:citrus-context.xml", 
                                   "classpath:com/consol/citrus/functions/citrus-function-ctx.xml"})
public abstract class AbstractBaseTest extends AbstractTestNGSpringContextTests {
    protected TestContext context;
    
    @Autowired
    FunctionRegistry functionRegistry;
    
    @Autowired
    GlobalVariables globalVariables;
    
    @BeforeMethod
    public void setup() {
        context = new TestContext();
        
        context.setFunctionRegistry(functionRegistry);
        context.setGlobalVariables(globalVariables);
    }
    
    protected TestContext createTestContext() {
        TestContext newContext = new TestContext();
        
        newContext.setFunctionRegistry(functionRegistry);
        newContext.setGlobalVariables(globalVariables);
        
        return newContext;
    }
}
