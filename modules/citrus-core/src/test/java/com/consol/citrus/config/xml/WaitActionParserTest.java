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

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.WaitAction;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.condition.HttpCondition;
import com.consol.citrus.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitActionParserTest extends AbstractActionParserTest<WaitAction> {

    @Test
    public void testWaitActionParser() {
        String httpUrl = "http://some.url/";
        String filePath = "/some/path";

        assertActionCount(4);
        assertActionClassAndName(WaitAction.class, "wait");

        WaitAction action = getNextTestActionFromTest();
        Condition condition = getFileCondition(filePath);
        validateWaitAction(action, WaitAction.DEFAULT_WAIT_TIME, WaitAction.DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
        validateWaitAction(action, "10", "2", condition);

        action = getNextTestActionFromTest();
        condition = getHttpCondition(httpUrl, HttpCondition.DEFAULT_RESPONSE_CODE, HttpCondition.DEFAULT_TIMEOUT);
        validateWaitAction(action, WaitAction.DEFAULT_WAIT_TIME, WaitAction.DEFAULT_INTERVAL, condition);

        action = getNextTestActionFromTest();
        condition = getHttpCondition(httpUrl, "503", "2");
        validateWaitAction(action, WaitAction.DEFAULT_WAIT_TIME, WaitAction.DEFAULT_INTERVAL, condition);
    }

    private Condition getFileCondition(String path) {
        FileCondition condition = new FileCondition();
        condition.setFilename(path);
        return condition;
    }

    private Condition getHttpCondition(String url, String responseCode, String timeout) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(url);
        condition.setHttpResponseCode(responseCode);
        condition.setTimeoutSeconds(timeout);
        return condition;
    }

    private void validateWaitAction(WaitAction action, String expectedWaitTime, String expectedWaitInterval, Condition expectedCondition) {
        Assert.assertEquals(action.getWaitForSeconds(), expectedWaitTime);
        Assert.assertEquals(action.getTestIntervalSeconds(), expectedWaitInterval);
        Condition condition = action.getCondition();
        Assert.assertNotNull(condition);
        Assert.assertEquals(condition, expectedCondition);
    }
}
