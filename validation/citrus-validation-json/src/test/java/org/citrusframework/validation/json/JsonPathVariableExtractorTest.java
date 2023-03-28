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

package org.citrusframework.validation.json;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathVariableExtractorTest extends AbstractTestNGUnitTest {

    private Message jsonMessage;

    @BeforeClass
    public void setup() {
        jsonMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"numbers\": [10, 20, 30, 40], \"id\":\"x123456789x\"}");
    }

    @Test
    public void testExtractVariables() throws Exception {
        JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor.Builder()
                .expression("$['index']", "index")
                .expression("$.numbers", "numbers")
                .expression("$.numbers.size()", "numbersSize")
                .expression("$.person", "person")
                .expression("$.person.name", "personName")
                .expression("$.toString()", "toString")
                .expression("$.keySet()", "keySet")
                .expression("$.values()", "values")
                .expression("$.size()", "size")
                .expression("$.*", "all")
                .expression("$", "root")
            .build();
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
