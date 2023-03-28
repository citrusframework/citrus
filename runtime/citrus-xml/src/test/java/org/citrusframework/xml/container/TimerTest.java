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

package org.citrusframework.xml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.container.Timer;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TimerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadAsync() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/timer-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "TimerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);

        Assert.assertEquals(result.getTestAction(0).getClass(), Timer.class);

        int actionIndex = 0;

        Timer action = (Timer) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimerId(), "timer1");
        Assert.assertEquals(action.getDelay(), 5000L);
        Assert.assertEquals(action.getRepeatCount(), 1);
        Assert.assertEquals(action.getInterval(), 2000L);
        Assert.assertEquals(action.getActionCount(), 1);

        action = (Timer) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimerId(), "timer2");
        Assert.assertEquals(action.getDelay(), 500L);
        Assert.assertEquals(action.getRepeatCount(), 2);
        Assert.assertEquals(action.getInterval(), 200L);
        Assert.assertEquals(action.getActionCount(), 2);

        long defaultDelay = 0L;
        int defaultRepeat = Integer.MAX_VALUE;
        long defaultInterval = 1000L;

        action = (Timer) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getTimerId());
        Assert.assertEquals(action.getDelay(), defaultDelay);
        Assert.assertEquals(action.getRepeatCount(), defaultRepeat);
        Assert.assertEquals(action.getInterval(), defaultInterval);
        Assert.assertEquals(action.getActionCount(), 1);
    }

}
