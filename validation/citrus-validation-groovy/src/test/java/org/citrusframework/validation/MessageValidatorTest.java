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
import org.citrusframework.validation.script.GroovyJsonMessageValidator;
import org.citrusframework.validation.script.GroovyScriptMessageValidator;
import org.citrusframework.validation.script.GroovyXmlMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultGroovyTextMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyTextMessageValidator").getClass(), GroovyScriptMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGroovyJsonMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyJsonMessageValidator").getClass(), GroovyJsonMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultGroovyXmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultGroovyXmlMessageValidator").getClass(), GroovyXmlMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-text").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-json").isPresent());
        Assert.assertTrue(MessageValidator.lookup("groovy-xml").isPresent());
    }
}
