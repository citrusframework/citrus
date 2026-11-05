/*
 * Copyright the original author or authors.
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

package org.citrusframework.xml.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.condition.ActionCondition;
import org.citrusframework.api.condition.Condition;
import org.citrusframework.condition.FileCondition;
import org.citrusframework.condition.HttpCondition;
import org.citrusframework.condition.MessageCondition;
import org.citrusframework.container.Wait;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageStore;
import org.citrusframework.message.MessageStore;
import org.citrusframework.util.TestUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.citrusframework.util.TestUtils.HTTPS_CITRUSFRAMEWORK_ORG;

public class WaitForTest extends AbstractXmlActionTest {

    private static final String DEFAULT_WAIT_TIME = "5000";
    private static final String DEFAULT_INTERVAL = "1000";
    private static final String DEFAULT_TIMEOUT = "1000";
    private static final String DEFAULT_RESPONSE_CODE = "200";

    @Test
    public void shouldLoadWaitFor() {

        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because citrus is not reachable. We are probably running behind a proxy.");
        }

        String filePath = "classpath:org/citrusframework/xml/test-request-payload.xml";

        MessageStore messageStore = new DefaultMessageStore();
        messageStore.storeMessage("request", new DefaultMessage("Citrus rocks!"));
        context.setMessageStore(messageStore);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/container/wait-for.citrus.it.xml");

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
        condition = getHttpCondition(HTTPS_CITRUSFRAMEWORK_ORG, DEFAULT_RESPONSE_CODE, DEFAULT_TIMEOUT);
        validateWaitAction(action, DEFAULT_WAIT_TIME, DEFAULT_INTERVAL, condition);

        action = (Wait) result.getTestAction(actionIndex++);
        condition = getHttpCondition(HTTPS_CITRUSFRAMEWORK_ORG + "/doesnotexist", "404", "2000");
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

        switch (expectedCondition) {
            case HttpCondition httpCondition -> {
                HttpCondition condition = (HttpCondition) action.getCondition();
                Assert.assertNotNull(condition);
                Assert.assertEquals(condition.getName(), expectedCondition.getName());
                Assert.assertEquals(condition.getUrl(), httpCondition.getUrl());
                Assert.assertEquals(condition.getTimeout(), httpCondition.getTimeout());
                Assert.assertEquals(condition.getMethod(), httpCondition.getMethod());
            }
            case FileCondition fileCondition -> {
                FileCondition condition = (FileCondition) action.getCondition();
                Assert.assertNotNull(condition);
                Assert.assertEquals(condition.getName(), expectedCondition.getName());
                Assert.assertEquals(condition.getFilePath(), fileCondition.getFilePath());
            }
            case MessageCondition messageCondition -> {
                MessageCondition condition = (MessageCondition) action.getCondition();
                Assert.assertNotNull(condition);
                Assert.assertEquals(condition.getName(), expectedCondition.getName());
                Assert.assertEquals(condition.getMessageName(), messageCondition.getMessageName());
            }
            case ActionCondition actionCondition -> {
                ActionCondition condition = (ActionCondition) action.getCondition();
                Assert.assertNotNull(condition);
                Assert.assertEquals(condition.getAction().getName(), actionCondition.getAction().getName());
                Assert.assertEquals(((EchoAction) condition.getAction()).getMessage(), ((EchoAction) actionCondition.getAction()).getMessage());
            }
            default -> Assert.fail("Unexpected condition type: " + expectedCondition.getClass());
        }
    }
}
