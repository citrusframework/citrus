/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.validation;

import java.io.IOException;
import java.util.Collections;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class SimpleSoapAttachmentValidatorTest {

    @Test
    public void testSimpleValidation() throws IOException {
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");

        SoapMessage testMessage = new SoapMessage("Some Payload")
                                    .addAttachment(controlAttachment);

        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, Collections.singletonList(controlAttachment));
    }

    @Test
    public void testSimpleValidationNoControlContentId() throws IOException {
        SoapAttachment receivedAttachment = new SoapAttachment();
        receivedAttachment.setContentId("soapAttachmentId");
        receivedAttachment.setContentType("text/plain");
        receivedAttachment.setContent("This is a test!");

        SoapMessage testMessage = new SoapMessage("Some Payload")
                                    .addAttachment(receivedAttachment);

        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");

        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, Collections.singletonList(controlAttachment));
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testSimpleValidationWrongContentId() throws IOException {
        SoapAttachment receivedAttachment = new SoapAttachment();
        receivedAttachment.setContentId("soapAttachmentId");
        receivedAttachment.setContentType("text/plain");
        receivedAttachment.setContent("This is a test!");

        SoapMessage testMessage = new SoapMessage("Some Payload")
                                    .addAttachment(receivedAttachment);

        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("wrongAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");

        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, Collections.singletonList(controlAttachment));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Values not equal for attachment content 'soapAttachmentId', expected 'ThisisnotOK!' but was 'Thisisatest!'")
    public void testSimpleValidationWrongContent() throws IOException {
        SoapAttachment receivedAttachment = new SoapAttachment();
        receivedAttachment.setContentId("soapAttachmentId");
        receivedAttachment.setContentType("text/plain");
        receivedAttachment.setContent("This is a test!");

        SoapMessage testMessage = new SoapMessage("Some Payload")
                                    .addAttachment(receivedAttachment);

        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is not OK!");

        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, Collections.singletonList(controlAttachment));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Values not equal for attachment contentType, expected 'text/xml' but was 'text/plain'")
    public void testSimpleValidationWrongContentType() throws IOException {
        SoapAttachment receivedAttachment = new SoapAttachment();
        receivedAttachment.setContentId("soapAttachmentId");
        receivedAttachment.setContentType("text/plain");
        receivedAttachment.setContent("This is a test!");

        SoapMessage testMessage = new SoapMessage("Some Payload")
                                    .addAttachment(receivedAttachment);

        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/xml");
        controlAttachment.setContent("This is a test!");

        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, Collections.singletonList(controlAttachment));
    }
}
