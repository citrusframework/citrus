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

package org.citrusframework.xml.message;

import org.citrusframework.message.MessageProcessor;
import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.validation.xml.XpathMessageProcessor;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XmlMessageProcessorTest {

    @Test
    public void testLookup() {
        Assertions.assertTrue(MessageProcessor.lookup("xpathExtract").isPresent());
        Assertions.assertEquals(XpathPayloadVariableExtractor.Builder.class, MessageProcessor.lookup("xpathExtract").get().getClass());
        Assertions.assertTrue(MessageProcessor.lookup("xmlValidate", (GenericValidationProcessor<Object>) (payload, headers, context) -> {}).isPresent());
        Assertions.assertEquals(XmlMarshallingValidationProcessor.Builder.class, MessageProcessor.lookup("xmlValidate", (GenericValidationProcessor<Object>) (payload, headers, context) -> {}).get().getClass());
        Assertions.assertTrue(MessageProcessor.lookup("xpath").isPresent());
        Assertions.assertEquals(XpathMessageProcessor.Builder.class, MessageProcessor.lookup("xpath").get().getClass());
    }
}
