/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.validation.json;

import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathVariableExtractorTest extends AbstractTestNGUnitTest {

    private JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor();
    private Message jsonMessage;

    @BeforeClass
    public void setup() {
        jsonMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"numbers\": [10, 20, 30, 40], \"id\":\"x123456789x\"}");
    }

    @Test
    public void testExtractVariables() throws Exception {
        variableExtractor.getJsonPathExpressions().put("$['index']", "index");
        variableExtractor.getJsonPathExpressions().put("$.numbers", "numbers");
        variableExtractor.getJsonPathExpressions().put("$.numbers.size()", "numbersSize");
        variableExtractor.getJsonPathExpressions().put("$.person", "person");
        variableExtractor.getJsonPathExpressions().put("$.person.name", "personName");
        variableExtractor.getJsonPathExpressions().put("$.toString()", "toString");
        variableExtractor.getJsonPathExpressions().put("$.keySet()", "keySet");
        variableExtractor.getJsonPathExpressions().put("$.values()", "values");
        variableExtractor.getJsonPathExpressions().put("$.size()", "size");
        variableExtractor.getJsonPathExpressions().put("$.*", "all");
        variableExtractor.getJsonPathExpressions().put("$", "root");
        variableExtractor.extractVariables(jsonMessage, context);

        Assert.assertNotNull(context.getVariable("toString"));
        Assert.assertEquals(context.getVariable("toString"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"numbers\":[10,20,30,40],\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertNotNull(context.getVariable("keySet"));
        Assert.assertEquals(context.getVariable("keySet"), "[person, numbers, index, text, id]");
        Assert.assertNotNull(context.getVariable("values"));
        Assert.assertEquals(context.getVariable("values"), "[{\"surname\":\"Doe\",\"name\":\"John\"}, [10,20,30,40], 5, Hello World!, x123456789x]");
        Assert.assertNotNull(context.getVariable("size"));
        Assert.assertEquals(context.getVariable("size"), "5");
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertEquals(context.getVariable("person"), "{\"surname\":\"Doe\",\"name\":\"John\"}");
        Assert.assertNotNull(context.getVariable("personName"));
        Assert.assertEquals(context.getVariable("personName"), "John");
        Assert.assertNotNull(context.getVariable("index"));
        Assert.assertEquals(context.getVariable("index"), "5");

        Assert.assertNotNull(context.getVariable("numbers"));
        Assert.assertEquals(context.getVariable("numbers"), "[10,20,30,40]");

        Assert.assertNotNull(context.getVariable("numbersSize"));
        Assert.assertEquals(context.getVariable("numbersSize"), "4");

        Assert.assertNotNull(context.getVariable("all"));
        Assert.assertEquals(context.getVariable("all"), "[{\"surname\":\"Doe\",\"name\":\"John\"},[10,20,30,40],5,\"Hello World!\",\"x123456789x\"]");
        Assert.assertNotNull(context.getVariable("root"));
        Assert.assertEquals(context.getVariable("root"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"numbers\":[10,20,30,40],\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
    }
}