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

package org.citrusframework;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.xhtml.XhtmlMessageValidator;
import org.citrusframework.validation.xhtml.XhtmlXpathMessageValidator;
import org.citrusframework.validation.xml.DomXmlMessageValidator;
import org.citrusframework.validation.xml.XpathMessageValidator;
import org.testng.Assert;

public abstract class UnitTestSupport extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xml", new DomXmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xpath", new XpathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtml", new XhtmlMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("xhtmlXpath", new XhtmlXpathMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("plaintext", new DefaultMessageValidator() {
            @Override
            public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ValidationContext validationContexts) {
                Assert.assertEquals(receivedMessage.getPayload(String.class), controlMessage.getPayload());
            }

            @Override
            public boolean supportsMessageType(String messageType, Message message) {
                return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.name());
            }
        });

        return factory;
    }
}
