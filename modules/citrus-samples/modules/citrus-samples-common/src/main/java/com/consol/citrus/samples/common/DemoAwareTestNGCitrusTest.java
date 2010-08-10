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
