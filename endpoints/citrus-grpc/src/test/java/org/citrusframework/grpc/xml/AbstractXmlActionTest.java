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
package org.citrusframework.grpc.xml;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.base.context.StaticTestContextFactory;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@Isolated("CitrusInstanceManager publishes a shared static Citrus instance on every test method")
public class AbstractXmlActionTest extends AbstractUnitTest {

    protected Citrus citrus;

    @Mock
    protected CitrusContext citrusContext;

    @BeforeEach
    @Override
    public void prepareTest() {
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);

        super.prepareTest();
    }

    @Override
    protected TestContext createTestContext() {
        TestContext context = super.createTestContext();
        lenient().when(citrusContext.getReferenceResolver()).thenReturn(context.getReferenceResolver());
        lenient().when(citrusContext.getMessageValidatorRegistry()).thenReturn(context.getMessageValidatorRegistry());
        lenient().when(citrusContext.getTestContextFactory()).thenReturn(new StaticTestContextFactory(context));
        lenient().doAnswer(invocationOnMock -> {
            CitrusAnnotations.parseConfiguration(invocationOnMock.getArgument(0, Object.class), citrusContext);
            return null;
        }).when(citrusContext).parseConfiguration((Object) any());
        lenient().doAnswer(invocationOnMock -> {
            context.getReferenceResolver().bind(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }).when(citrusContext).addComponent(anyString(), any());
        CitrusAnnotations.injectAll(this, citrus, context);
        return context;
    }

    protected XmlTestLoader createTestLoader(String sourcePath) {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new DefaultTestCaseRunner(context));
        testLoader.setSource(TestSourceHelper.create(sourcePath));

        return testLoader;
    }
}
