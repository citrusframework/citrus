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

package com.consol.citrus.testng;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.HtmlReporter;
import com.consol.citrus.report.JUnitReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract base testng test implementation for Citrus unit testing. Provides access to
 * a test context and injected function registry as well as global variables.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractTestNGUnitTest extends AbstractTestNGCitrusTest {
    /** Test context */
    protected TestContext context;

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactory testContextFactory;

    @Autowired
    private HtmlReporter htmlReporter;

    @Autowired
    private JUnitReporter jUnitReporter;

    static {
        System.setProperty(Citrus.DEFAULT_APPLICATION_CONTEXT_PROPERTY, "classpath:com/consol/citrus/context/citrus-unit-context.xml");
    }

    /**
     * Setup test execution.
     */
    @BeforeMethod
    public void prepareTest() {
        htmlReporter.setEnabled(false);
        jUnitReporter.setEnabled(false);
        context = createTestContext();
    }

    /**
     * Creates the test context with global variables and function registry.
     * @return
     */
    protected TestContext createTestContext() {
        try {
            return testContextFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create test context", e);
        }
    }
}
