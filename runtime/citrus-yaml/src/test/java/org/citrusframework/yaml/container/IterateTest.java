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

package org.citrusframework.yaml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Iterate;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.AbstractYamlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class IterateTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadIterate() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/container/iterate-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "IterateTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);

        Assert.assertEquals(result.getTestAction(0).getClass(), Iterate.class);

        Iterate action = (Iterate) result.getTestAction(0);
        Assert.assertEquals(action.getCondition(), "i < 3");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStep(), 1);
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getActionCount(), 1L);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);

        action = (Iterate) result.getTestAction(1);
        Assert.assertEquals(result.getTestAction(0).getClass(), Iterate.class);
        Assert.assertEquals(action.getCondition(), "index <= 2");
        Assert.assertEquals(action.getIndexName(), "index");
        Assert.assertEquals(action.getStep(), 1);
        Assert.assertEquals(action.getStart(), 1);
        Assert.assertEquals(action.getActionCount(), 1L);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);

        action = (Iterate) result.getTestAction(2);
        Assert.assertEquals(action.getCondition(), "i <= 10");
        Assert.assertEquals(action.getIndexName(), "i");
        Assert.assertEquals(action.getStep(), 5);
        Assert.assertEquals(action.getStart(), 0);
        Assert.assertEquals(action.getActionCount(), 2L);
        Assert.assertEquals(action.getTestAction(0).getClass(), EchoAction.class);
        Assert.assertEquals(action.getTestAction(1).getClass(), EchoAction.class);
    }

}
