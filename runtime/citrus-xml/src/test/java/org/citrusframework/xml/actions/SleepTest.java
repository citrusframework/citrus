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

package org.citrusframework.xml.actions;

import java.util.concurrent.TimeUnit;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SleepTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadSleep() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/sleep-test.xml");

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
}
