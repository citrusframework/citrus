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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assertions.assertEquals(1L, validators.size());
        Assertions.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assertions.assertEquals(DefaultMessageHeaderValidator.class, validators.get("defaultMessageHeaderValidator").getClass());
    }

    @Test
    public void testTestLookup() {
        Assertions.assertTrue(MessageValidator.lookup("header").isPresent());
    }
}
