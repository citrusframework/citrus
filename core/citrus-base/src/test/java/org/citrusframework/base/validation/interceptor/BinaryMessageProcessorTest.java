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

package org.citrusframework.base.validation.interceptor;

import java.nio.charset.StandardCharsets;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.interceptor.BinaryMessageProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinaryMessageProcessorTest extends UnitTestSupport {

    private final BinaryMessageProcessor processor = new BinaryMessageProcessor();

    @Test
    public void testBinaryMessageStaysUntouched(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo".getBytes(StandardCharsets.UTF_8));
        message.setType(MessageType.BINARY);

        //WHEN
        processor.process(message, context);

        //THEN
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), message.getPayload(byte[].class));
    }

    @Test
    public void testTextMessageIsIntercepted(){

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), message.getPayload(byte[].class));
        assertEquals(MessageType.BINARY.name(), message.getType());
    }

    @Test
    public void testResourceMessageWithIsIntercepted() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertArrayEquals(FileUtils.copyToByteArray(getTestFile().getInputStream()), message.getPayload(byte[].class));
        assertEquals(MessageType.BINARY.name(), message.getType());
    }

    @Test
    public void testMessageResourceNotFound() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(Resources.fromFileSystem("unknown.txt"));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        assertThrows(CitrusRuntimeException.class, () -> processor.process(message, context));

        //THEN should throw exception
    }

    private Resource getTestFile() {
        return Resources.create("foo.txt", BinaryMessageProcessorTest.class);
    }
}
