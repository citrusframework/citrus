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

package org.citrusframework;

import java.util.Collections;

import org.citrusframework.container.AfterSuite;
import org.citrusframework.container.BeforeSuite;
import org.citrusframework.container.SequenceAfterSuite;
import org.citrusframework.container.SequenceBeforeSuite;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.LogModifier;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.report.TestReporters;
import org.citrusframework.report.TestSuiteListener;
import org.citrusframework.report.TestSuiteListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class TestSuiteTest extends AbstractTestNGUnitTest {

    @Mock
    private TestSuiteListeners testSuiteListeners;
    @Mock
    private TestSuiteListener testSuiteListener;
    @Mock
    private ApplicationContext applicationContextMock;
    @Mock
    private TestContextFactoryBean testContextFactory;
    @Mock
    private TestAction actionMock;

    private Citrus citrus;

    private BeforeSuite beforeActions;
    private AfterSuite afterActions;

    @BeforeMethod
    public void resetMocks() {
        MockitoAnnotations.openMocks(this);

        testSuiteListeners = new TestSuiteListeners();
        testSuiteListeners.addTestSuiteListener(testSuiteListener);
        beforeActions = new SequenceBeforeSuite();
        afterActions = new SequenceAfterSuite();

        when(testContextFactory.getObject()).thenReturn(context);

        when(applicationContextMock.getBean(FunctionRegistry.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(ValidationMatcherRegistry.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(MessageValidatorRegistry.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(MessageListeners.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(TestReporters.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(ReferenceResolver.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(TypeConverter.class)).thenThrow(NoSuchBeanDefinitionException.class);
        when(applicationContextMock.getBean(LogModifier.class)).thenThrow(NoSuchBeanDefinitionException.class);

        when(applicationContextMock.getBean(TestContextFactoryBean.class)).thenReturn(testContextFactory);
        when(applicationContextMock.getBeansOfType(AfterSuite.class)).thenReturn(Collections.singletonMap("afterActions", afterActions));
        when(applicationContextMock.getBeansOfType(BeforeSuite.class)).thenReturn(Collections.singletonMap("beforeActions", beforeActions));
        when(applicationContextMock.getBean(TestSuiteListeners.class)).thenReturn(testSuiteListeners);
        when(applicationContextMock.getBean(TestListeners.class)).thenReturn(new TestListeners());

        citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContextMock));
    }

    @Test
    public void testBeforeSuite() {
        beforeActions.addTestAction(actionMock);

        citrus.beforeSuite("sample-suite");

        verify(actionMock).execute(any(TestContext.class));
        verify(testSuiteListener).onStart();
        verify(testSuiteListener).onStartSuccess();
    }

    @Test
    public void testFailBeforeSuite() {
        doThrow(new CitrusRuntimeException("Failed!")).when(actionMock).execute(any(TestContext.class));

        beforeActions.addTestAction(actionMock);

        try {
            citrus.beforeSuite("sample-suite");
        } catch (AssertionError e) {
            verify(actionMock).execute(any(TestContext.class));
            verify(testSuiteListener).onStart();
            verify(testSuiteListener).onStartFailure(any(Throwable.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing before suite action");
    }

    @Test
    public void testFailBeforeSuiteWithAfterSuite() {
        TestAction afterSuiteAction = Mockito.mock(TestAction.class);
        afterActions.addTestAction(afterSuiteAction);

        doThrow(new CitrusRuntimeException("Failed!")).when(actionMock).execute(any(TestContext.class));
        beforeActions.addTestAction(actionMock);

        try {
            citrus.beforeSuite("sample-suite");
        } catch (AssertionError e) {
            verify(testSuiteListener).onStart();
            verify(testSuiteListener).onStartFailure(any(Throwable.class));
            verify(testSuiteListener).onFinish();
            verify(testSuiteListener).onFinishSuccess();
            verify(actionMock).execute(any(TestContext.class));
            verify(afterSuiteAction).execute(any(TestContext.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing before suite action");
    }

    @Test
    public void testAfterSuite() {
        afterActions.addTestAction(actionMock);

        citrus.afterSuite("sample-suite");

        verify(actionMock).execute(any(TestContext.class));
        verify(testSuiteListener).onFinish();
        verify(testSuiteListener).onFinishSuccess();
    }

    @Test
    public void testFailAfterSuite() {
        doThrow(new CitrusRuntimeException("Failed!")).when(actionMock).execute(any(TestContext.class));
        afterActions.addTestAction(actionMock);

        try {
            citrus.afterSuite("sample-suite");
        } catch (AssertionError e) {
            verify(actionMock).execute(any(TestContext.class));
            verify(testSuiteListener).onFinish();
            verify(testSuiteListener).onFinishFailure(any(Throwable.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing after suite action");
    }

    @Test
    public void testBeforeTest() {
        SequenceBeforeTest beforeTestActions = new SequenceBeforeTest();

        beforeTestActions.addTestAction(actionMock);
        beforeTestActions.execute(context);

        verify(actionMock).execute(any(TestContext.class));
    }

    @Test
    public void testBeforeTestFail() {
        SequenceBeforeTest beforeTestActions = new SequenceBeforeTest();

        doThrow(new CitrusRuntimeException("Failed!")).when(actionMock).execute(any(TestContext.class));
        beforeTestActions.addTestAction(actionMock);

        try {
            beforeTestActions.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed!");
            verify(actionMock).execute(any(TestContext.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing before suite action");
    }
}
