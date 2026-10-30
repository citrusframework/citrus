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

package org.citrusframework.spring;

import org.citrusframework.base.report.HtmlReporter;
import org.citrusframework.base.report.JUnitReporter;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.spring.context.TestContextFactoryBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(CitrusSuiteExtension.class)
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class UnitTestSupport extends AbstractUnitTest {

    /** Factory bean for test context */
    @Autowired
    protected TestContextFactoryBean testContextFactory;

    @Autowired(required = false)
    private HtmlReporter htmlReporter;

    @Autowired(required = false)
    private JUnitReporter jUnitReporter;

    @BeforeEach
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
