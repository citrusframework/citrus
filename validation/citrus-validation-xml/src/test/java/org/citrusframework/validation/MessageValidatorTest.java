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
import org.citrusframework.validation.xhtml.XhtmlMessageValidator;
import org.citrusframework.validation.xhtml.XhtmlXpathMessageValidator;
import org.citrusframework.validation.xml.DomXmlMessageValidator;
import org.citrusframework.validation.xml.XpathMessageValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageValidatorTest {

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 8L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultXmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultXmlMessageValidator").getClass(), DomXmlMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXpathMessageValidator"));
        Assert.assertEquals(validators.get("defaultXpathMessageValidator").getClass(), XpathMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXhtmlMessageValidator"));
        Assert.assertEquals(validators.get("defaultXhtmlMessageValidator").getClass(), XhtmlMessageValidator.class);
        Assert.assertNotNull(validators.get("defaultXhtmlXpathMessageValidator"));
        Assert.assertEquals(validators.get("defaultXhtmlXpathMessageValidator").getClass(), XhtmlXpathMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xml").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xpath").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xhtml").isPresent());
        Assert.assertTrue(MessageValidator.lookup("xhtml-xpath").isPresent());
    }
}
