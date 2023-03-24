/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.config.xml;

import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Wait;
import org.citrusframework.condition.*;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitParserTest extends AbstractActionParserTest<Wait> {

    private static final String DEFAULT_WAIT_TIME = "5000";
    private static final String DEFAULT_INTERVAL = "1000";
    private static final String DEFAULT_TIMEOUT = "1000";
    private static final String DEFAULT_RESPONSE_CODE = "200";

    @Test
    public void testWaitActionParser() {
        String httpUrl = "http://some.url/";
        String filePath = "/some/path";

        assertActionCount(6);
        assertActionClassAndName(Wait.class, "wait");

        Wait action = getNextTestActionFromTest();
        Condition condition = getFileCondition(filePath);
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
        validateWaitAction(action, "10000", "2000", condition);

        action = getNextTestActionFromTest();
        condition = getHttpCondition(httpUrl, DEFAULT_RESPONSE_CODE, DEFAULT_TIMEOUT);
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
        condition = getHttpCondition(httpUrl, "503", "2000");
        ((HttpCondition)condition).setMethod("GET");
        validateWaitAction(action, "3000", DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
        condition = getMessageCondition("request");
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
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
