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

package org.citrusframework.mail.server;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.mail.MailSettings;
import org.citrusframework.mail.model.MailMarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MailServerBuilderTest extends AbstractUnitTest {

    @Test
    public void shouldApplyMarshallerType() {
        MailServer server = new MailServerBuilder()
                .port(25000)
                .marshallerType("json")
                .build();

        Assertions.assertEquals("json", server.getMarshaller().getType());
    }

    @Test
    public void shouldApplyMarshallerTypeCaseInsensitive() {
        MailServer server = new MailServerBuilder()
                .port(25000)
                .marshallerType("JSON")
                .build();

        Assertions.assertEquals("JSON", server.getMarshaller().getType());
    }

    @Test
    public void shouldUseDefaultMarshallerType() {
        MailServer server = new MailServerBuilder()
                .port(25000)
                .build();

        MailMarshaller marshaller = server.getMarshaller();
        Assertions.assertNotNull(marshaller);
        Assertions.assertEquals(MailSettings.getMarshallerType(), marshaller.getType());
    }

    @Test
    public void shouldRejectMarshallerAndMarshallerType() {
        MailServerBuilder builder = new MailServerBuilder()
                .port(25000)
                .marshallerType("json");
        builder.setMarshaller("myMarshaller");

        CitrusRuntimeException exception = Assertions.assertThrows(CitrusRuntimeException.class, builder::build);

        Assertions.assertTrue(exception.getMessage().contains("marshaller"));
        Assertions.assertTrue(exception.getMessage().contains("marshallerType"));
    }

    @Test
    public void shouldRejectUnknownMarshallerType() {
        MailServerBuilder builder = new MailServerBuilder()
                .port(25000)
                .marshallerType("yaml");

        CitrusRuntimeException exception = Assertions.assertThrows(CitrusRuntimeException.class, builder::build);

        Assertions.assertTrue(exception.getMessage().contains("XML"));
        Assertions.assertTrue(exception.getMessage().contains("JSON"));
    }
}
