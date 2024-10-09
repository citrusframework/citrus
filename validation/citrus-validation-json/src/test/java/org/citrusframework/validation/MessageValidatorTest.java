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

package org.citrusframework.validation;

import java.util.Map;

import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidator;
import org.citrusframework.validation.json.JsonTextMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultJsonMessageValidator"));
        Assert.assertEquals(validators.get("defaultJsonMessageValidator").getClass(), JsonTextMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultJsonPathMessageValidator"));
        Assert.assertEquals(validators.get("defaultJsonPathMessageValidator").getClass(), JsonPathMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("json").isPresent());
        Assert.assertTrue(MessageValidator.lookup("json-path").isPresent());
    }
}
