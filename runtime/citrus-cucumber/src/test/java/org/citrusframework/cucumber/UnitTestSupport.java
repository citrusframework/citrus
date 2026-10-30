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

package org.citrusframework.cucumber;

import org.citrusframework.Citrus;
import org.citrusframework.spring.CitrusSpringContextProvider;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.spring.context.TestContextFactoryBean;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.base.report.HtmlReporter;
import org.citrusframework.base.report.JUnitReporter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CitrusSpringConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UnitTestSupport {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private HtmlReporter htmlReporter;

    @Autowired
    private JUnitReporter jUnitReporter;

    /** Factory for test context */
    protected TestContextFactory contextFactory;

    /** Test context */
    protected TestContext context;

    /** Citrus instance */
    protected Citrus citrus;

    @BeforeAll
    public void beforeSuite() {
        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        citrus.beforeSuite(getClass().getSimpleName());
    }

    /**
     * Runs tasks after test suite.
     */
    @AfterAll
    public void afterSuite() {
        if (citrus != null) {
            citrus.afterSuite(getClass().getSimpleName());
        }
    }

    @BeforeEach
    public void prepareTest() {
        htmlReporter.setEnabled(false);
        jUnitReporter.setEnabled(false);
        contextFactory = createTestContextFactory();
        context = createTestContext();
    }

    protected TestContext createTestContext() {
        try {
            return contextFactory.getObject();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create test context", e);
        }
    }

    protected TestContextFactory createTestContextFactory() {
        return testContextFactory;
    }
}
