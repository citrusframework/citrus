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

package com.consol.citrus.yaml.container;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.RepeatOnErrorUntilTrue;
import com.consol.citrus.yaml.YamlTestLoader;
import com.consol.citrus.yaml.actions.AbstractYamlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnErrorTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadRepeatOnError() {
        YamlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/yaml/container/repeat-on-error-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "RepeatOnErrorTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);

        Assert.assertEquals(result.getTestAction(0).getClass(), RepeatOnErrorUntilTrue.class);

        RepeatOnErrorUntilTrue action = (RepeatOnErrorUntilTrue) result.getTestAction(0);
        Assert.assertEquals(action.getCondition(), "i > 3");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(1000L));
        Assert.assertEquals(action.getActionCount(), 1);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);

        action = (RepeatOnErrorUntilTrue) result.getTestAction(1);
        Assert.assertEquals(action.getCondition(), "index >= 2");
        Assert.assertEquals(action.getIndexName(), "index");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(1000L));
        Assert.assertEquals(action.getActionCount(), 1);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);

        action = (RepeatOnErrorUntilTrue) result.getTestAction(2);
        Assert.assertEquals(action.getCondition(), "i >= 10");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(500L));
        Assert.assertEquals(action.getActionCount(), 2);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(action.getTestAction(1).getClass(), EchoAction.class);

        action = (RepeatOnErrorUntilTrue) result.getTestAction(3);
        Assert.assertEquals(action.getCondition(), "i >= 5");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getAutoSleep(), Long.valueOf(250L));
        Assert.assertEquals(action.getActionCount(), 1);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);
    }

}
