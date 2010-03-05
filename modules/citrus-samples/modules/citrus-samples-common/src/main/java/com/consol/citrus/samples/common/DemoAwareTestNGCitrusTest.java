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

package com.consol.citrus.samples.common;

import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.consol.citrus.samples.CitrusSamplesDemo;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * @author Christoph Deppisch
 */
public abstract class DemoAwareTestNGCitrusTest extends AbstractTestNGCitrusTest {
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        getDemo().start();
        
        super.beforeSuite(testContext);
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        super.afterSuite(testContext);
        
        getDemo().stop();
    }
    
    public abstract CitrusSamplesDemo getDemo();
}
