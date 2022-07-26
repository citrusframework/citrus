/*
 * Copyright 2021 the original author or authors.
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

package com.consol.citrus.xml;

import java.util.concurrent.TimeUnit;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XmlTestLoaderTest extends AbstractTestNGUnitTest {

    @Mock
    private CitrusContext citrusContext;

    private Citrus citrus;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
        citrus = Citrus.newInstance(() -> citrusContext);
    }

    @Test
    public void shouldLoadEcho() {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new DefaultTestCaseRunner(context));
        testLoader.setSource("classpath:com/consol/citrus/xml/echo-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "EchoTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction) result.getTestAction(0)).getMessage(), "Hello from Citrus!");
    }

    @Test
    public void shouldLoadSleep() {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new DefaultTestCaseRunner(context));
        testLoader.setSource("classpath:com/consol/citrus/xml/sleep-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SleepTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SleepAction.class);
        Assert.assertEquals(((SleepAction) result.getTestAction(0)).getTime(), "500");
        Assert.assertEquals(((SleepAction) result.getTestAction(0)).getTimeUnit(), TimeUnit.MILLISECONDS);
    }

    @Test
    public void shouldLoadCreateVariables() {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), "Test", this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new DefaultTestCaseRunner(context));
        testLoader.setSource("classpath:com/consol/citrus/xml/create-variables-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateVariablesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().size(), 4L);
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var1"), "test1");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var2"), "test2");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var3"), "test3");
        Assert.assertEquals(((CreateVariablesAction) result.getTestAction(0)).getVariables().get("var4"), "script:<groovy>return \"test4\"");
    }
}
