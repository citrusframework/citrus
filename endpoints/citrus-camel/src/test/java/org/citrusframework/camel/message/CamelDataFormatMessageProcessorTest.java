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

package org.citrusframework.camel.message;

import java.util.Base64;

import org.citrusframework.CitrusSettings;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.TypeConversionUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import static org.citrusframework.camel.message.CamelDataFormatMessageProcessor.Builder.marshal;
import static org.citrusframework.camel.message.CamelDataFormatMessageProcessor.Builder.unmarshal;

@Isolated("Mutates global TypeConverter state via System properties")
public class CamelDataFormatMessageProcessorTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    private final CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    public void setupTypeConverter() {
        System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, "camel");
        TypeConversionUtils.loadDefaultConverter();
    }

    @AfterAll
    public static void restoreTypeConverterDefault() {
        System.setProperty(CitrusSettings.TYPE_CONVERTER_PROPERTY, CitrusSettings.TYPE_CONVERTER_DEFAULT);
        TypeConversionUtils.loadDefaultConverter();
    }

    @Test
    public void shouldMarshalFromProcessor() {
        CamelDataFormatMessageProcessor messageProcessor = marshal()
                .base64()
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage("Hello from Citrus!")
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals(new String(in.getPayload(byte[].class)).trim(),
                new String(Base64.getEncoder().encode("Hello from Citrus!".getBytes())));
        Assertions.assertEquals("sayHello", in.getHeader("operation"));
    }

    @Test
    public void shouldUnmarshalFromProcessor() {
        CamelDataFormatMessageProcessor messageProcessor = unmarshal()
                .base64()
                .camelContext(camelContext)
                .build();

        Message in = new DefaultMessage(Base64.getEncoder().encode("Hello from Citrus!".getBytes()))
                .setHeader("operation", "sayHello");
        messageProcessor.process(in, context);

        Assertions.assertEquals("Hello from Citrus!", in.getPayload(String.class));
        Assertions.assertEquals("sayHello", in.getHeader("operation"));
    }
}
