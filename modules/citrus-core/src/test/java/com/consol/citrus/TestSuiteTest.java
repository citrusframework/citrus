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

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class TestSuiteTest extends AbstractTestNGUnitTest {

    private TestSuiteListeners testSuiteListeners;
    private TestSuiteListener testSuiteListener = Mockito.mock(TestSuiteListener.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
    private Citrus citrus;

    private SequenceBeforeSuite beforeActions;
    private SequenceAfterSuite afterActions;

    @BeforeMethod
    public void resetMocks() {
        reset(testSuiteListener);
        reset(applicationContextMock);

        testSuiteListeners = new TestSuiteListeners();
        testSuiteListeners.addTestSuiteListener(testSuiteListener);
        beforeActions = new SequenceBeforeSuite();
        afterActions = new SequenceAfterSuite();

        when(applicationContextMock.getBean(TestContextFactory.class)).thenReturn(testContextFactory);
        when(applicationContextMock.getBeansOfType(SequenceAfterSuite.class)).thenReturn(Collections.singletonMap("afterActions", afterActions));
        when(applicationContextMock.getBeansOfType(SequenceBeforeSuite.class)).thenReturn(Collections.singletonMap("beforeActions", beforeActions));
        when(applicationContextMock.getBean(TestSuiteListeners.class)).thenReturn(testSuiteListeners);
        when(applicationContextMock.getBean(TestListeners.class)).thenReturn(new TestListeners());

        citrus = Citrus.newInstance(applicationContextMock);
    }

    @Test
    public void testBeforeSuite() {
        beforeActions.addTestAction(new EchoAction.Builder().build());

        citrus.beforeSuite("sample-suite");

        verify(testSuiteListener).onStart();
        verify(testSuiteListener).onStartSuccess();
    }

    @Test
    public void testFailBeforeSuite() {
        beforeActions.addTestAction(new FailAction.Builder().build());

        try {
            citrus.beforeSuite("sample-suite");
        } catch (AssertionError e) {
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

        beforeActions.addTestAction(new FailAction.Builder().build());

        try {
            citrus.beforeSuite("sample-suite");
        } catch (AssertionError e) {
            verify(testSuiteListener).onStart();
            verify(testSuiteListener).onStartFailure(any(Throwable.class));
            verify(testSuiteListener).onFinish();
            verify(testSuiteListener).onFinishSuccess();
            verify(afterSuiteAction).execute(any(TestContext.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing before suite action");
    }

    @Test
    public void testAfterSuite() {
        afterActions.addTestAction(new EchoAction.Builder().build());

        citrus.afterSuite("sample-suite");

        verify(testSuiteListener).onFinish();
        verify(testSuiteListener).onFinishSuccess();
    }

    @Test
    public void testFailAfterSuite() {
        afterActions.addTestAction(new FailAction.Builder().build());

        try {
            citrus.afterSuite("sample-suite");
        } catch (AssertionError e) {
            verify(testSuiteListener).onFinish();
            verify(testSuiteListener).onFinishFailure(any(Throwable.class));
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to failing after suite action");
    }

    @Test
    public void testBeforeTest() {
        SequenceBeforeTest beforeTestActions = new SequenceBeforeTest();

        beforeTestActions.addTestAction(new EchoAction.Builder().build());
        beforeTestActions.execute(createTestContext());
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testBeforeTestFail() {
        SequenceBeforeTest beforeTestActions = new SequenceBeforeTest();

        beforeTestActions.addTestAction(new FailAction.Builder().build());
        beforeTestActions.execute(createTestContext());
    }
}
