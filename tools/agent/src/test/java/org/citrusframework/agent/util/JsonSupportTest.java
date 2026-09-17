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

package org.citrusframework.agent.util;

import java.time.Duration;
import java.util.List;

import org.citrusframework.TestResult;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;
import tools.jackson.databind.JsonNode;

public class JsonSupportTest {

    @Test
    public void shouldSerializeThrowableAsString() {
        CitrusRuntimeException cause = new CitrusRuntimeException("Something went wrong");
        TestResult failedResult = TestResult.failed("myTest", "com.example.MyTest", cause);

        String json = JsonSupport.render(List.of(failedResult));

        JsonNode tree = JsonSupport.json().readTree(json);
        JsonNode causeNode = tree.get(0).get("cause");
        Assert.assertTrue(causeNode.isString(),
                "Expected 'cause' to be a JSON string, but was: " + causeNode.getNodeType());
        Assert.assertEquals(causeNode.stringValue(),
                "org.citrusframework.exceptions.CitrusRuntimeException - Something went wrong");
    }

    @Test
    public void shouldSerializeDurationAsNumber() {
        TestResult result = TestResult.success("myTest", "com.example.MyTest")
                .withDuration(Duration.ofMillis(1234));

        String json = JsonSupport.render(List.of(result));

        JsonNode tree = JsonSupport.json().readTree(json);
        JsonNode durationNode = tree.get(0).get("duration");
        Assert.assertTrue(durationNode.isNumber(),
                "Expected 'duration' to be a JSON number, but was: " + durationNode.getNodeType());
        Assert.assertEquals(durationNode.intValue(), 1234);
    }

    @Test
    public void shouldSerializeSuccessfulResult() {
        TestResult result = TestResult.success("myTest", "com.example.MyTest");

        String json = JsonSupport.render(List.of(result));

        JsonNode tree = JsonSupport.json().readTree(json);
        JsonNode resultNode = tree.get(0);
        Assert.assertEquals(resultNode.get("testName").stringValue(), "myTest");
        Assert.assertEquals(resultNode.get("className").stringValue(), "com.example.MyTest");
        Assert.assertEquals(resultNode.get("result").stringValue(), "SUCCESS");
    }
}
