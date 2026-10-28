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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.interceptor.GzipMessageProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GzipMessageProcessorTest extends UnitTestSupport {

    private final GzipMessageProcessor processor = new GzipMessageProcessor();

    @Test
    public void testGzipMessageStaysUntouched() throws IOException {
        try (ByteArrayOutputStream zipped = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(zipped)) {
                gzipOutputStream.write("foo".getBytes(StandardCharsets.UTF_8));

                //GIVEN
                final DefaultMessage message = new DefaultMessage(gzipOutputStream);
                message.setType(MessageType.GZIP);

                //WHEN
                processor.process(message, context);

                //THEN
                assertEquals(gzipOutputStream, message.getPayload());
            }
        }
    }

    @Test
    public void testTextMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo");
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(MessageType.GZIP.name(), message.getType());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)))) {
            unzipped.write(gzipInputStream.readAllBytes());
            assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), unzipped.toByteArray());
        }
    }

    @Test
    public void testBinaryMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage("foo".getBytes(StandardCharsets.UTF_8));
        message.setType(MessageType.BINARY);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(MessageType.GZIP.name(), message.getType());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)))) {
            unzipped.write(gzipInputStream.readAllBytes());
            assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), unzipped.toByteArray());
        }
    }

    @Test
    public void testInputStreamMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(new ByteArrayInputStream("foo".getBytes()));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(MessageType.GZIP.name(), message.getType());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)))) {
            unzipped.write(gzipInputStream.readAllBytes());
            assertArrayEquals("foo".getBytes(StandardCharsets.UTF_8), unzipped.toByteArray());
        }
    }

    @Test
    public void testResourceMessageIsIntercepted() throws IOException {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(getTestFile());
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        processor.process(message, context);

        //THEN
        assertEquals(MessageType.GZIP.name(), message.getType());
        try (ByteArrayOutputStream unzipped = new ByteArrayOutputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(message.getPayload(byte[].class)))) {
            unzipped.write(gzipInputStream.readAllBytes());
            assertArrayEquals(FileUtils.copyToByteArray(getTestFile().getInputStream()), unzipped.toByteArray());
        }
    }

    @Test
    public void testProcessMessageResourceNotFound() {

        //GIVEN
        final DefaultMessage message = new DefaultMessage(Resources.fromFileSystem("unknown.txt"));
        message.setType(MessageType.PLAINTEXT);

        //WHEN
        assertThrows(CitrusRuntimeException.class, () -> processor.process(message, context));

        //THEN should throw exception
    }

    private Resource getTestFile() {
        return Resources.create("foo.txt", GzipMessageProcessorTest.class);
    }
}
