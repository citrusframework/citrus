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

package org.citrusframework.message;

import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.citrusframework.validation.json.JsonPathMessageProcessor;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonMessageProcessorTest {

    @Test
    public void testLookup() {
        Assert.assertTrue(MessageProcessor.lookup("jsonExtract").isPresent());
        Assert.assertEquals(MessageProcessor.lookup("jsonExtract").get().getClass(), JsonPathVariableExtractor.Builder.class);
        Assert.assertTrue(MessageProcessor.lookup("jsonValidate", JsonMessageProcessorTest.class).isPresent());
        Assert.assertEquals(MessageProcessor.lookup("jsonValidate", JsonMessageProcessorTest.class).get().getClass(), JsonMappingValidationProcessor.Builder.class);
        Assert.assertTrue(MessageProcessor.lookup("jsonPath").isPresent());
        Assert.assertEquals(MessageProcessor.lookup("jsonPath").get().getClass(), JsonPathMessageProcessor.Builder.class);
    }
}
