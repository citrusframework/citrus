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
import org.citrusframework.validation.text.BinaryBase64MessageValidator;
import org.citrusframework.validation.text.GzipBinaryBase64MessageValidator;
import org.citrusframework.validation.text.PlainTextMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultBinaryBase64MessageValidator"));
        Assert.assertEquals(validators.get("defaultBinaryBase64MessageValidator").getClass(), BinaryBase64MessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGzipBinaryBase64MessageValidator"));
        Assert.assertEquals(validators.get("defaultGzipBinaryBase64MessageValidator").getClass(), GzipBinaryBase64MessageValidator.class);
        Assert.assertNotNull(validators.get("defaultPlaintextMessageValidator"));
        Assert.assertEquals(validators.get("defaultPlaintextMessageValidator").getClass(), PlainTextMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("binary_base64").isPresent());
        Assert.assertTrue(MessageValidator.lookup("gzip_base64").isPresent());
        Assert.assertTrue(MessageValidator.lookup("plaintext").isPresent());
    }
}
