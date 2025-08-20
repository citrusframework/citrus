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

import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.validation.xml.XpathMessageProcessor;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XmlMessageProcessorTest {

    @Test
    public void testLookup() {
        Assert.assertTrue(MessageProcessor.lookup("xpathExtract").isPresent());
        Assert.assertEquals(MessageProcessor.lookup("xpathExtract").get().getClass(), XpathPayloadVariableExtractor.Builder.class);
        Assert.assertTrue(MessageProcessor.lookup("xmlValidate", (GenericValidationProcessor<Object>) (payload, headers, context) -> {}).isPresent());
        Assert.assertEquals(MessageProcessor.lookup("xmlValidate", (GenericValidationProcessor<Object>) (payload, headers, context) -> {}).get().getClass(), XmlMarshallingValidationProcessor.Builder.class);
        Assert.assertTrue(MessageProcessor.lookup("xpath").isPresent());
        Assert.assertEquals(MessageProcessor.lookup("xpath").get().getClass(), XpathMessageProcessor.Builder.class);
    }
}
