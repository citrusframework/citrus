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

package com.consol.citrus;

import java.util.Collections;

import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.BeforeSuite;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
