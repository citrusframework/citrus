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

package org.citrusframework.base.message;

import java.util.List;

import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointBuilder;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessageStoreTest implements TestActionSupport {

    private final DirectEndpoint directEndpoint = new DirectEndpointBuilder()
            .queue(new DefaultMessageQueue("foo"))
            .build();

    private TestContext context;
    private TestCaseRunner t;

    @BeforeEach
    public void createTestContext() {
        context = TestContextFactory.newInstance().getObject();
        t = TestCaseRunnerFactory.createRunner(context);

        context.getMessageValidatorRegistry().addMessageValidator("simple", new MessageValidator<>() {
            @Override
            public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, List<ValidationContext> validationContexts) throws ValidationException {
                Assertions.assertEquals(controlMessage.getPayload(), receivedMessage.getPayload());
                validationContexts.forEach(ctx -> ctx.updateStatus(ValidationStatus.PASSED));
            }

            @Override
            public boolean supportsMessageType(String messageType, Message message) {
                return true;
            }
        });
    }

    @Test
    public void shouldStoreMessages() {
        t.run(send()
                .endpoint(directEndpoint)
                .message()
                .name("request")
                .body("Citrus rocks!"));

        Assertions.assertNotNull(context.getMessageStore().getMessage("request"));
        Assertions.assertEquals("Citrus rocks!", context.getMessageStore().getMessage("request").getPayload(String.class));

        t.run(receive()
                .endpoint(directEndpoint)
                .message()
                .name("response")
                .body("Citrus rocks!"));

        Assertions.assertNotNull(context.getMessageStore().getMessage("response"));
        Assertions.assertEquals("Citrus rocks!", context.getMessageStore().getMessage("response").getPayload(String.class));
    }

    @Test
    public void shouldStoreMessagesFromValidationCallback() {
        t.run(send()
                .endpoint(directEndpoint)
                .message()
                .name("request")
                .body("Citrus is awesome!"));

        Assertions.assertNotNull(context.getMessageStore().getMessage("request"));
        Assertions.assertEquals("Citrus is awesome!", context.getMessageStore().getMessage("request").getPayload(String.class));

        t.run(receive()
                .endpoint(directEndpoint)
                .message()
                .name("response")
                .validate((message, context) -> {
                    Message request = context.getMessageStore().getMessage("request");
                    Assertions.assertEquals(request.getPayload(), message.getPayload());
                }));

        Assertions.assertNotNull(context.getMessageStore().getMessage("response"));
        Assertions.assertEquals("Citrus is awesome!", context.getMessageStore().getMessage("response").getPayload(String.class));
    }
}
