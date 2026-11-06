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

package org.citrusframework.groovy.dsl.container;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.condition.ActionCondition;
import org.citrusframework.api.condition.Condition;
import org.citrusframework.condition.FileCondition;
import org.citrusframework.condition.HttpCondition;
import org.citrusframework.condition.MessageCondition;
import org.citrusframework.container.Wait;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.dsl.AbstractGroovyActionDslTest;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageStore;
import org.citrusframework.message.MessageStore;
import org.citrusframework.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.util.TestUtils.HTTPS_CITRUSFRAMEWORK_ORG;

public class WaitForTest extends AbstractGroovyActionDslTest {

    private static final String DEFAULT_WAIT_TIME = "5000";
    private static final String DEFAULT_INTERVAL = "1000";
    private static final String DEFAULT_TIMEOUT = "1000";
    private static final String DEFAULT_RESPONSE_CODE = "200";

    @Test
    public void shouldLoadWaitFor() {

        if (!TestUtils.isNetworkReachable()) {
            Assumptions.abort("Test skipped because citrus is not reachable. We are probably running behind a proxy.");
        }

        String filePath = "classpath:org/citrusframework/groovy/test-request-payload.xml";

        MessageStore messageStore = new DefaultMessageStore();
        messageStore.storeMessage("request", new DefaultMessage("Citrus rocks!"));
        context.setMessageStore(messageStore);

        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/container/wait-for.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("WaitForTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(6L, result.getActionCount());

        Assertions.assertEquals(Wait.class, result.getTestAction(0).getClass());

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
        Assertions.assertEquals(expectedMilliseconds, action.getTime());
        Assertions.assertEquals(expectedInterval, action.getInterval());

        if (!(expectedCondition instanceof ActionCondition)) {
            Assertions.assertEquals(expectedCondition.getClass(), action.getCondition().getClass());
        }

        switch (expectedCondition) {
            case HttpCondition httpCondition -> {
                HttpCondition condition = (HttpCondition) action.getCondition();
                Assertions.assertNotNull(condition);
                Assertions.assertEquals(expectedCondition.getName(), condition.getName());
                Assertions.assertEquals(httpCondition.getUrl(), condition.getUrl());
                Assertions.assertEquals(httpCondition.getTimeout(), condition.getTimeout());
                Assertions.assertEquals(httpCondition.getMethod(), condition.getMethod());
            }
            case FileCondition fileCondition -> {
                FileCondition condition = (FileCondition) action.getCondition();
                Assertions.assertNotNull(condition);
                Assertions.assertEquals(expectedCondition.getName(), condition.getName());
                Assertions.assertEquals(fileCondition.getFilePath(), condition.getFilePath());
            }
            case MessageCondition messageCondition -> {
                MessageCondition condition = (MessageCondition) action.getCondition();
                Assertions.assertNotNull(condition);
                Assertions.assertEquals(expectedCondition.getName(), condition.getName());
                Assertions.assertEquals(messageCondition.getMessageName(), condition.getMessageName());
            }
            case ActionCondition actionCondition -> {
                ActionCondition condition = (ActionCondition) action.getCondition();
                Assertions.assertNotNull(condition);
                Assertions.assertEquals(actionCondition.getAction().getName(), condition.getAction().getName());
                Assertions.assertEquals(((EchoAction) actionCondition.getAction()).getMessage(), ((EchoAction) condition.getAction()).getMessage());
            }
            default -> Assertions.fail("Unexpected condition type: " + expectedCondition.getClass());
        }
    }
}
