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

package org.citrusframework.groovy.dsl;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class EchoTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadEcho() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/echo.test.groovy");

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
    public void shouldLoadPrint() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/print.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "PrintTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction) result.getTestAction(0)).getMessage(), "Hello from Citrus!");
        Assert.assertEquals(result.getTestAction(1).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction) result.getTestAction(1)).getMessage(), "Hello");
    }
}
