/*
 * Copyright the original author or authors.
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

package org.citrusframework;

import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

@ContextConfiguration(classes = CitrusSpringConfig.class)
public class UnitTestSupport extends AbstractTestNGUnitTest {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired(required = false)
    private HtmlReporter htmlReporter;

    @Autowired(required = false)
    private JUnitReporter jUnitReporter;

    /** Citrus instance */
    protected Citrus citrus;

    @BeforeSuite(alwaysRun = true)
    @Override
    public void beforeSuite() throws Exception {
        super.beforeSuite();

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
    }

    /**
     * Runs tasks after test suite.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (citrus != null) {
            citrus.afterSuite(Reporter.getCurrentTestResult().getTestContext().getSuite().getName(),
                    Reporter.getCurrentTestResult().getTestContext().getIncludedGroups());
        }
    }

    @BeforeMethod
    @Override
    public void prepareTest() {
        if (htmlReporter != null) {
            htmlReporter.setEnabled(false);
        }

        if (jUnitReporter != null) {
            jUnitReporter.setEnabled(false);
        }
        super.prepareTest();
    }

    @Override
    protected TestContext createTestContext() {
        try {
            return super.createTestContext();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create test context", e);
        }
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        return testContextFactory;
    }
}
