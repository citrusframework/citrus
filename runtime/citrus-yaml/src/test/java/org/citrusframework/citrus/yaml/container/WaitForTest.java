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

package org.citrusframework.citrus.yaml.container;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.TestCaseMetaInfo;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.condition.ActionCondition;
import org.citrusframework.citrus.condition.Condition;
import org.citrusframework.citrus.condition.FileCondition;
import org.citrusframework.citrus.condition.HttpCondition;
import org.citrusframework.citrus.condition.MessageCondition;
import org.citrusframework.citrus.container.Wait;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.DefaultMessageStore;
import org.citrusframework.citrus.message.MessageStore;
import org.citrusframework.citrus.yaml.YamlTestLoader;
import org.citrusframework.citrus.yaml.actions.AbstractYamlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class WaitForTest extends AbstractYamlActionTest {

    private static final String DEFAULT_WAIT_TIME = "5000";
    private static final String DEFAULT_INTERVAL = "1000";
    private static final String DEFAULT_TIMEOUT = "1000";
    private static final String DEFAULT_RESPONSE_CODE = "200";

    @Test
    public void shouldLoadWaitFor() {
        String httpUrl = "https://citrusframework.org";
        String filePath = "classpath:org/citrusframework/citrus/yaml/test-request-payload.xml";

        MessageStore messageStore = new DefaultMessageStore();
        messageStore.storeMessage("request", new DefaultMessage("Citrus rocks!"));
        context.setMessageStore(messageStore);

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/citrus/yaml/container/wait-for-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "WaitForTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 6L);

        Assert.assertEquals(result.getTestAction(0).getClass(), Wait.class);

        int actionIndex = 0;

        Wait action = (Wait) result.getTestAction(actionIndex++);
        Condition condition = getFileCondition(filePath);
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = (Wait) result.getTestAction(actionIndex++);
        validateWaitAction(action, "10000", "2000", condition);

        action = (Wait) result.getTestAction(actionIndex++);
        condition = getHttpCondition(httpUrl, DEFAULT_RESPONSE_CODE, DEFAULT_TIMEOUT);
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = (Wait) result.getTestAction(actionIndex++);
        condition = getHttpCondition(httpUrl + "/doesnotexist", "404", "2000");
        ((HttpCondition)condition).setMethod("GET");
        validateWaitAction(action, "3000", DEFAULT_INTERVAL, condition);

        action = (Wait) result.getTestAction(actionIndex++);
        condition = getMessageCondition("request");
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = (Wait) result.getTestAction(actionIndex);
        condition = getActionCondition();
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);
    }

    private Condition getFileCondition(String path) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(path);
        return condition;
    }

    private Condition getMessageCondition(String name) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(name);
        return condition;
    }

    private Condition getActionCondition() {
        return new ActionCondition(new EchoAction.Builder().message("Citrus rocks!").build());
    }

    private Condition getHttpCondition(String url, String responseCode, String timeout) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(url);
        condition.setHttpResponseCode(responseCode);
        condition.setTimeout(timeout);
        return condition;
    }

    private void validateWaitAction(Wait action, String expectedMilliseconds, String expectedInterval, Condition expectedCondition) {
        Assert.assertEquals(action.getTime(), expectedMilliseconds);
        Assert.assertEquals(action.getInterval(), expectedInterval);

        if (!(expectedCondition instanceof ActionCondition)) {
            Assert.assertEquals(action.getCondition().getClass(), expectedCondition.getClass());
        }

        if (expectedCondition instanceof HttpCondition) {
            HttpCondition condition = (HttpCondition) action.getCondition();
            Assert.assertNotNull(condition);
            Assert.assertEquals(condition.getName(), expectedCondition.getName());
            Assert.assertEquals(condition.getUrl(), ((HttpCondition) expectedCondition).getUrl());
            Assert.assertEquals(condition.getTimeout(), ((HttpCondition) expectedCondition).getTimeout());
            Assert.assertEquals(condition.getMethod(), ((HttpCondition) expectedCondition).getMethod());
        } else if (expectedCondition instanceof FileCondition) {
            FileCondition condition = (FileCondition) action.getCondition();
            Assert.assertNotNull(condition);
            Assert.assertEquals(condition.getName(), expectedCondition.getName());
            Assert.assertEquals(condition.getFilePath(), ((FileCondition) expectedCondition).getFilePath());
        } else if (expectedCondition instanceof MessageCondition) {
            MessageCondition condition = (MessageCondition) action.getCondition();
            Assert.assertNotNull(condition);
            Assert.assertEquals(condition.getName(), expectedCondition.getName());
            Assert.assertEquals(condition.getMessageName(), ((MessageCondition) expectedCondition).getMessageName());
        } else if (expectedCondition instanceof ActionCondition) {
            ActionCondition condition = (ActionCondition) action.getCondition();
            Assert.assertNotNull(condition);
            Assert.assertEquals(condition.getAction().getName(), ((ActionCondition) expectedCondition).getAction().getName());
            Assert.assertEquals(((EchoAction) condition.getAction()).getMessage(), ((EchoAction)((ActionCondition) expectedCondition).getAction()).getMessage());
        }
    }

}
