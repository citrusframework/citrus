/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.ws.message;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.easymock.EasyMock;
import org.springframework.ws.mime.Attachment;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.nio.charset.Charset;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SoapAttachmentTest {

    private Attachment attachment = EasyMock.createMock(Attachment.class);

    @Test
    public void testFromAttachment() throws Exception {
        reset(attachment);

        expect(attachment.getContentId()).andReturn("mail").once();
        expect(attachment.getContentType()).andReturn("text/plain").times(2);
        expect(attachment.getInputStream()).andReturn(new StaticTextDataSource("This is mail text content!", "text/plain", "UTF-8", "mail").getInputStream()).once();

        replay(attachment);

        SoapAttachment soapAttachment = SoapAttachment.from(attachment);

        Assert.assertEquals(soapAttachment.getContentId(), "mail");
        Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
        Assert.assertEquals(soapAttachment.getContent(), "This is mail text content!");
        Assert.assertEquals(soapAttachment.getCharsetName(), Charset.defaultCharset().displayName());
        Assert.assertNotNull(soapAttachment.getDataHandler());
        Assert.assertEquals(soapAttachment.getSize(), 26L);

        verify(attachment);
    }

    @Test
    public void testFromBinaryAttachment() throws Exception {
        reset(attachment);

        expect(attachment.getContentId()).andReturn("img").once();
        expect(attachment.getContentType()).andReturn("application/octet-stream").times(2);

        expect(attachment.getDataHandler()).andReturn(new DataHandler(new StaticTextDataSource("This is img text content!", "application/octet-stream", "UTF-8", "img")));

        replay(attachment);

        SoapAttachment soapAttachment = SoapAttachment.from(attachment);

        Assert.assertEquals(soapAttachment.getContentId(), "img");
        Assert.assertEquals(soapAttachment.getContentType(), "application/octet-stream");
        Assert.assertEquals(soapAttachment.getContent(), Base64.encodeBase64String("This is img text content!".getBytes(Charset.forName("UTF-8"))));
        Assert.assertEquals(soapAttachment.getCharsetName(), Charset.defaultCharset().displayName());
        Assert.assertNotNull(soapAttachment.getDataHandler());
        Assert.assertEquals(soapAttachment.getSize(), 25L);

        soapAttachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        Assert.assertEquals(soapAttachment.getContent(), Base64.encodeBase64String("This is img text content!".getBytes(Charset.forName("UTF-8"))));

        soapAttachment.setEncodingType(SoapAttachment.ENCODING_HEX_BINARY);
        Assert.assertEquals(soapAttachment.getContent(), Hex.encodeHexString("This is img text content!".getBytes(Charset.forName("UTF-8"))).toUpperCase());

        verify(attachment);
    }

    @Test
    public void testFileResourceTextContent() throws Exception {
        SoapAttachment soapAttachment = new SoapAttachment();
        soapAttachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");
        soapAttachment.setContentType("text/xml");

        Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        Assert.assertNotNull(soapAttachment.getDataHandler());
        Assert.assertEquals(soapAttachment.getSize(), 64L);
    }

    @Test
    public void testFileResourceBinaryContent() throws Exception {
        SoapAttachment soapAttachment = new SoapAttachment();
        soapAttachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");
        soapAttachment.setContentType("image/png");

        Assert.assertEquals(soapAttachment.getContent(), Base64.encodeBase64String("<TestAttachment><Message>Hello World!</Message></TestAttachment>".getBytes(Charset.forName("UTF-8"))));
        Assert.assertNotNull(soapAttachment.getDataHandler());
        Assert.assertEquals(soapAttachment.getSize(), 64L);
    }

    private class StaticTextDataSource implements DataSource {

        private final String content;
        private final String contentType;
        private final String charsetName;
        private final String contentId;

        private StaticTextDataSource(String content, String contentType, String charsetName, String contentId) {
            this.content = content;
            this.contentType = contentType;
            this.charsetName = charsetName;
            this.contentId = contentId;
        }

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
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}