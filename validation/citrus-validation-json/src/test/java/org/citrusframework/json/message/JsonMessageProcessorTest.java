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

package org.citrusframework.json.message;

import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.citrusframework.validation.json.JsonPathMessageProcessor;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonMessageProcessorTest {

    @Test
    public void testLookup() {
        Assertions.assertTrue(MessageProcessor.lookup("jsonExtract").isPresent());
        Assertions.assertEquals(JsonPathVariableExtractor.Builder.class, MessageProcessor.lookup("jsonExtract").get().getClass());
        Assertions.assertTrue(MessageProcessor.lookup("jsonValidate", JsonMessageProcessorTest.class).isPresent());
        Assertions.assertEquals(JsonMappingValidationProcessor.Builder.class, MessageProcessor.lookup("jsonValidate", JsonMessageProcessorTest.class).get().getClass());
        Assertions.assertTrue(MessageProcessor.lookup("jsonPath").isPresent());
        Assertions.assertEquals(JsonPathMessageProcessor.Builder.class, MessageProcessor.lookup("jsonPath").get().getClass());
    }
}
