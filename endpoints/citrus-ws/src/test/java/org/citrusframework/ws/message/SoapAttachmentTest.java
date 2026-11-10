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

package org.citrusframework.ws.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.mockito.Mockito;
import org.springframework.ws.mime.Attachment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SoapAttachmentTest {

    private final Attachment attachment = Mockito.mock(Attachment.class);

    @Test
    public void testFromAttachment() throws Exception {
        reset(attachment);

        when(attachment.getContentId()).thenReturn("mail");
        when(attachment.getContentType()).thenReturn("text/plain");
        when(attachment.getInputStream()).thenReturn(new StaticTextDataSource("This is mail text content!", "text/plain", "UTF-8", "mail").getInputStream());

        SoapAttachment soapAttachment = SoapAttachment.from(attachment);

        Assertions.assertEquals("mail", soapAttachment.getContentId());
        Assertions.assertEquals("text/plain", soapAttachment.getContentType());
        Assertions.assertEquals("This is mail text content!", soapAttachment.getContent());
        Assertions.assertEquals(soapAttachment.getCharsetName(), Charset.defaultCharset().displayName());
        Assertions.assertNotNull(soapAttachment.getDataHandler());
        Assertions.assertEquals(26L, soapAttachment.getSize());

    }

    @Test
    public void testFromBinaryAttachment() {
        reset(attachment);

        when(attachment.getContentId()).thenReturn("img");
        when(attachment.getContentType()).thenReturn("application/octet-stream");

        when(attachment.getDataHandler()).thenReturn(new DataHandler(new StaticTextDataSource("This is img text content!", "application/octet-stream", "UTF-8", "img")));

        SoapAttachment soapAttachment = SoapAttachment.from(attachment);

        Assertions.assertEquals("img", soapAttachment.getContentId());
        Assertions.assertEquals("application/octet-stream", soapAttachment.getContentType());
        Assertions.assertEquals(soapAttachment.getContent(), Base64.encodeBase64String("This is img text content!".getBytes(StandardCharsets.UTF_8)));
        Assertions.assertEquals(soapAttachment.getCharsetName(), Charset.defaultCharset().displayName());
        Assertions.assertNotNull(soapAttachment.getDataHandler());
        Assertions.assertEquals(25L, soapAttachment.getSize());

        soapAttachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        Assertions.assertEquals(soapAttachment.getContent(), Base64.encodeBase64String("This is img text content!".getBytes(StandardCharsets.UTF_8)));

        soapAttachment.setEncodingType(SoapAttachment.ENCODING_HEX_BINARY);
        Assertions.assertEquals(soapAttachment.getContent(), Hex.encodeHexString("This is img text content!".getBytes(StandardCharsets.UTF_8)).toUpperCase());

    }

    @Test
    public void testFileResourceTextContent() {
        SoapAttachment soapAttachment = new SoapAttachment();
        soapAttachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment.xml");
        soapAttachment.setContentType("text/xml");

        Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", soapAttachment.getContent().trim());
        Assertions.assertNotNull(soapAttachment.getDataHandler());

        // Respect additional CR on win32 platforms
        Assertions.assertTrue(soapAttachment.getSize() == 65L || soapAttachment.getSize() == 66L);
    }

    @Test
    public void testFileResourceBinaryContent() throws Exception {
        String imageUrl = "org/citrusframework/ws/actions/test-attachment.png";

        SoapAttachment soapAttachment = new SoapAttachment();
        soapAttachment.setContentResourcePath("classpath:" + imageUrl);
        soapAttachment.setContentType("image/png");

        String attachmentContent = soapAttachment.getContent();
        byte[] resourceContent = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(imageUrl).toURI()));

        Assertions.assertEquals(attachmentContent, Base64.encodeBase64String(resourceContent));
        Assertions.assertEquals(resourceContent.length, soapAttachment.getSize());
    }

    private record StaticTextDataSource(String content, String contentType, String charsetName,
                                        String contentId) implements DataSource {

        @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content.getBytes(charsetName));
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public String getName() {
                return contentId;
            }

            @Override
            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        }
}
