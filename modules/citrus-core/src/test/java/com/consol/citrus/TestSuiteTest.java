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

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.container.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class TestSuiteTest extends AbstractTestNGUnitTest {
    @Autowired
    private TestSuiteListeners testSuiteListeners;

    @Autowired
    @Qualifier("mockListener")
    private TestSuiteListener testSuiteListener;

    @Autowired
    private TestContextFactory testContextFactory;

    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
    private Citrus citrus;

    private SequenceBeforeSuite beforeActions = new SequenceBeforeSuite();
    private SequenceAfterSuite afterActions = new SequenceAfterSuite();

    @BeforeClass
    public void setup() {
        when(applicationContextMock.getBean(TestContextFactory.class)).thenReturn(testContextFactory);
        when(applicationContextMock.getBeansOfType(SequenceAfterSuite.class)).thenReturn(Collections.singletonMap("afterActions", afterActions));
        when(applicationContextMock.getBeansOfType(SequenceBeforeSuite.class)).thenReturn(Collections.singletonMap("beforeActions", beforeActions));
        when(applicationContextMock.getBean(TestSuiteListeners.class)).thenReturn(testSuiteListeners);

        citrus = Citrus.newInstance(applicationContextMock);
    }

    @AfterClass
    public void cleanUpTest() {
        reset(testSuiteListener);
    }

    @Test
    public void testBeforeSuite() {
        reset(testSuiteListener);

        beforeActions.getActions().clear();
        beforeActions.addTestAction(new EchoAction());

        citrus.beforeSuite("sample-suite");

        verify(testSuiteListener).onStart();
        verify(testSuiteListener).onStartSuccess();
    }

    @Test
    public void testFailBeforeSuite() {
        reset(testSuiteListener);

        beforeActions.getActions().clear();
        beforeActions.addTestAction(new FailAction());

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
        afterActions.getActions().clear();
        afterActions.addTestAction(afterSuiteAction);
        
        reset(testSuiteListener, afterSuiteAction);

        beforeActions.getActions().clear();
        beforeActions.addTestAction(new FailAction());

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
        reset(testSuiteListener);

        afterActions.getActions().clear();
        afterActions.addTestAction(new EchoAction());

        citrus.afterSuite("sample-suite");

        verify(testSuiteListener).onFinish();
        verify(testSuiteListener).onFinishSuccess();
    }
    
    @Test
    public void testFailAfterSuite() {
        reset(testSuiteListener);

        afterActions.getActions().clear();
        afterActions.addTestAction(new FailAction());

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
        
        beforeTestActions.addTestAction(new EchoAction());
        beforeTestActions.execute(createTestContext());
    }
    
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testBeforeTestFail() {
        SequenceBeforeTest beforeTestActions = new SequenceBeforeTest();
        
        beforeTestActions.addTestAction(new FailAction());
        beforeTestActions.execute(createTestContext());
    }
}
