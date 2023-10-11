/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.validation.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GzipBinaryBase64MessageValidatorTest extends AbstractTestNGUnitTest {

    private final GzipBinaryBase64MessageValidator validator = new GzipBinaryBase64MessageValidator();
    private final ValidationContext validationContext = new DefaultValidationContext();

    @Test
    public void testGzipBinaryBase64Validation() throws IOException {
        Message receivedMessage = new DefaultMessage(getZippedContent("Hello World!"));
        Message controlMessage = new DefaultMessage(Base64.encodeBase64String("Hello World!".getBytes()));

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testGzipBinaryBase64ValidationNoBinaryData() throws IOException {
        Message receivedMessage = new DefaultMessage("SGVsbG8gV29ybGQh");
        Message controlMessage = new DefaultMessage(Base64.encodeBase64String("Hello World!".getBytes()));

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testGzipBinaryBase64ValidationError() throws IOException {
        Message receivedMessage = new DefaultMessage(getZippedContent("Hello World!"));
        Message controlMessage = new DefaultMessage(Base64.encodeBase64String("Hello Citrus!".getBytes()));

        try {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'SGVsbG8gQ2l0cnVzIQ=='"));
            Assert.assertTrue(e.getMessage().contains("but was 'SGVsbG8gV29ybGQh'"));

            return;
        }

        Assert.fail("Missing validation exception due to wrong number of JSON entries");
    }

    /**
     * Provide zipped content as byte array.
     * @param payload
     * @return
     * @throws IOException
     */
    private byte[] getZippedContent(String payload) throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
            gzipOutputStream.write(payload.getBytes());
            gzipOutputStream.flush();
            gzipOutputStream.close();
            return zipped.toByteArray();
        }
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new DefaultValidationContext());

        Assert.assertNotNull(validator.findValidationContext(validationContexts));

        validationContexts.clear();
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
