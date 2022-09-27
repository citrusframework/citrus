/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http.xml;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.CitrusInstanceManager;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.context.StaticTestContextFactory;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.xml.XmlTestLoader;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class AbstractXmlActionTest extends AbstractTestNGUnitTest {

    protected Citrus citrus;

    @Mock
    protected CitrusContext citrusContext;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);
    }

    @Override
    protected TestContext createTestContext() {
        TestContext context = super.createTestContext();
        when(citrusContext.getReferenceResolver()).thenReturn(context.getReferenceResolver());
        when(citrusContext.getMessageValidatorRegistry()).thenReturn(context.getMessageValidatorRegistry());
        when(citrusContext.getTestContextFactory()).thenReturn(new StaticTestContextFactory(context));
        CitrusAnnotations.injectAll(this, citrus, context);
        return context;
    }

    protected XmlTestLoader createTestLoader(String sourcePath) {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new NoopTestCaseRunner(context));
        testLoader.setSource(sourcePath);

        return testLoader;
    }

    protected static class NoopTestCaseRunner extends DefaultTestCaseRunner {
        public NoopTestCaseRunner(TestContext context) {
            super(context);
        }

        @Override
        public <T extends TestAction> T run(TestActionBuilder<T> builder) {
            return builder.build();
        }
    }
}
