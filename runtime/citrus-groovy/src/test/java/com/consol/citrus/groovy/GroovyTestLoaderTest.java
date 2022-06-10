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

package com.consol.citrus.groovy;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusContext;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyTestLoaderTest extends UnitTestSupport {

    private Citrus citrus;

    @BeforeMethod
    public void setup() {
        citrus = Citrus.newInstance(() -> CitrusContext.Builder.defaultContext().referenceResolver(context.getReferenceResolver()).build());
    }

    @Test
    public void shouldLoadGroovyTest() {
        TestCase test = getTestLoader("sample.test").load();

        Assert.assertEquals(test.getName(), "sample.test");
        Assert.assertEquals(test.getPackageName(), this.getClass().getPackageName() + ".dsl");
        Assert.assertEquals(test.getTestClass(), this.getClass());
        Assert.assertEquals(test.getActionCount(), 6L);
        Assert.assertEquals(((DefaultTestCase)test).getFinalActions().size(), 2L);
    }

    @Test
    public void shouldSupportJsonBuilderTest() {
        TestCase test = getTestLoader("json.test").load();

        Assert.assertEquals(test.getName(), "json.test");
        Assert.assertEquals(test.getPackageName(), this.getClass().getPackageName() + ".dsl");
        Assert.assertEquals(test.getTestClass(), this.getClass());
        Assert.assertEquals(test.getActionCount(), 3L);
    }

    @Test
    public void shouldSupportXmlMarkupBuilderTest() {
        TestCase test = getTestLoader("xml.test").load();

        Assert.assertEquals(test.getName(), "xml.test");
        Assert.assertEquals(test.getPackageName(), this.getClass().getPackageName() + ".dsl");
        Assert.assertEquals(test.getTestClass(), this.getClass());
        Assert.assertEquals(test.getActionCount(), 3L);
    }

    @Test
    public void shouldLookupTestLoader() {
        Assert.assertTrue(TestLoader.lookup().containsKey(TestLoader.GROOVY));
        Assert.assertTrue(TestLoader.lookup(TestLoader.GROOVY).isPresent());
        Assert.assertEquals(TestLoader.lookup(TestLoader.GROOVY).get().getClass(), GroovyTestLoader.class);
    }

    private TestLoader getTestLoader(String testName) {
        TestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.testClass(this.getClass());
        runner.name(testName);
        runner.packageName(this.getClass().getPackageName() + ".dsl");

        TestLoader testLoader = TestLoader.lookup(TestLoader.GROOVY).orElseThrow(() -> new CitrusRuntimeException("Missing Groovy test loader implementation"));
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, runner);
        return testLoader;
    }

}
