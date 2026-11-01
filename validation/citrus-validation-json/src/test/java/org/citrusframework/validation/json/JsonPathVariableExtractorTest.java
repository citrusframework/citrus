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

package org.citrusframework.validation.json;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @since 2.3
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonPathVariableExtractorTest extends AbstractUnitTest {

    private Message jsonMessage;

    @BeforeAll
    public void setup() {
        jsonMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"numbers\": [10, 20, 30, 40], \"id\":\"x123456789x\"}");
    }

    @Test
    public void testExtractVariables() {
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

        Assertions.assertNotNull(context.getVariable("toString"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"numbers\":[10,20,30,40],\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("toString"));
        Assertions.assertNotNull(context.getVariable("keySet"));
        Assertions.assertEquals("[person, numbers, index, text, id]", context.getVariable("keySet"));
        Assertions.assertNotNull(context.getVariable("values"));
        Assertions.assertEquals("[{\"surname\":\"Doe\",\"name\":\"John\"}, [10,20,30,40], 5, Hello World!, x123456789x]", context.getVariable("values"));
        Assertions.assertNotNull(context.getVariable("size"));
        Assertions.assertEquals("5", context.getVariable("size"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertEquals("{\"surname\":\"Doe\",\"name\":\"John\"}", context.getVariable("person"));
        Assertions.assertNotNull(context.getVariable("personName"));
        Assertions.assertEquals("John", context.getVariable("personName"));
        Assertions.assertNotNull(context.getVariable("index"));
        Assertions.assertEquals("5", context.getVariable("index"));

        Assertions.assertNotNull(context.getVariable("numbers"));
        Assertions.assertEquals("[10,20,30,40]", context.getVariable("numbers"));

        Assertions.assertNotNull(context.getVariable("numbersSize"));
        Assertions.assertEquals("4", context.getVariable("numbersSize"));

        Assertions.assertNotNull(context.getVariable("all"));
        Assertions.assertEquals("[{\"surname\":\"Doe\",\"name\":\"John\"},[10,20,30,40],5,\"Hello World!\",\"x123456789x\"]", context.getVariable("all"));
        Assertions.assertNotNull(context.getVariable("root"));
        Assertions.assertEquals("{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"numbers\":[10,20,30,40],\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}", context.getVariable("root"));
    }
}
