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

package org.citrusframework.ws.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.ws.mime.Attachment;

/**
 * Citrus SOAP attachment implementation.
 *
 * @author Christoph Deppisch
 */
public class SoapAttachment implements Attachment, Serializable {

    /** Serial */
    private static final long serialVersionUID = 6277464458242523954L;

    public static final String ENCODING_BASE64_BINARY = "base64Binary";
    public static final String ENCODING_HEX_BINARY = "hexBinary";

    /** Content body as string */
    private String content = null;

    /** Content body as file resource path  */
    private String contentResourcePath;

    /** Content type */
    private String contentType = "text/plain";

    /** Content identifier */
    private String contentId = null;

    /** Chosen charset of content body */
    private String charsetName = StandardCharsets.UTF_8.name();

    /** send mtom attachments inline as hex or base64 coded */
    private boolean mtomInline = false;

    /** Content data handler */
    private DataHandler dataHandler = null;

    /** Optional MTOM encoding */
    private String encodingType = ENCODING_BASE64_BINARY;

    /** Test context for variable resolving */
    private TestContext context;

    /**
     * Default constructor
     */
    public SoapAttachment() {
    }

    /**
     * Static construction method from Spring mime attachment.
     * @param attachment
     * @return
     */
    public static SoapAttachment from(Attachment attachment) {
        SoapAttachment soapAttachment = new SoapAttachment();

        String contentId = attachment.getContentId();
        if (contentId.startsWith("<") && contentId.endsWith(">")) {
            contentId = contentId.substring(1, contentId.length() - 1);
        }
        soapAttachment.setContentId(contentId);
        soapAttachment.setContentType(attachment.getContentType());

        if (attachment.getContentType().startsWith("text/") || attachment.getContentType().equals(MediaType.APPLICATION_XML_VALUE)) {
            try {
                soapAttachment.setContent(FileUtils.readToString(attachment.getInputStream()).trim());
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment content", e);
            }
        } else {
            // Binary content
            soapAttachment.setDataHandler(attachment.getDataHandler());
        }

        soapAttachment.setCharsetName(CitrusSettings.CITRUS_FILE_ENCODING);

        return soapAttachment;
    }

    /**
     * Constructor using fields.
     * @param content
     */
    public SoapAttachment(String content) {
        this.content = content;
    }

    /**
     * Constructor using fields.
     * @param contentId
     * @param contentType
     * @param content
     */
    public SoapAttachment(String contentId, String contentType, String content) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.content = content;
    }

    /**
     * Constructor using fields.
     * @param contentId
     * @param contentType
     * @param content
     * @param charsetName
     */
    public SoapAttachment(String contentId, String contentType, String content, String charsetName) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.content = content;
        this.charsetName = charsetName;
    }

    @Override
    public String getContentId() {
        if (contentId != null && context != null) {
            return context.replaceDynamicContentInString(contentId);
        } else {
            return contentId;
        }
    }

    @Override
    public String getContentType() {
        if (contentType != null && context != null) {
            return context.replaceDynamicContentInString(contentType);
        } else {
            return contentType;
        }
    }

    @Override
    public DataHandler getDataHandler() {
        if (dataHandler == null) {
            if (StringUtils.hasText(getContentResourcePath())) {
                dataHandler = new DataHandler(new FileResourceDataSource());
            } else {
                dataHandler = new DataHandler(new ContentDataSource());
            }
        }

        return dataHandler;
    }

    /**
     * Sets the data handler.
     * @param dataHandler
     */
    public void setDataHandler(DataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getDataHandler().getInputStream();
    }

    @Override
    public long getSize() {
        try {
            if (content != null) {
                return getContent().getBytes(charsetName).length;
            } else {
                return getSizeOfContent(getDataHandler().getInputStream());
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s [contentId: %s, contentType: %s, content: %s]", getClass().getSimpleName().toUpperCase(), getContentId(), getContentType(), getContent());
    }

    /**
     * Get the content body.
     * @return the content
     */
    public String getContent() {
        if (content != null) {
            return context != null ? context.replaceDynamicContentInString(content) : content;
        } else if (StringUtils.hasText(getContentResourcePath()) &&
                (getContentType().startsWith("text/") || getContentType().equals(MediaType.APPLICATION_XML_VALUE))) {
            try {
                String fileContent = FileUtils.readToString(Resources.create(getContentResourcePath()).getInputStream(), Charset.forName(charsetName));
                return context != null ? context.replaceDynamicContentInString(fileContent) : fileContent;
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment file resource", e);
            }
        } else {
            try {
                byte[] binaryData = FileUtils.copyToByteArray(getDataHandler().getInputStream());
                if (encodingType.equals(SoapAttachment.ENCODING_BASE64_BINARY)) {
                    return Base64.encodeBase64String(binaryData);
                } else if (encodingType.equals(SoapAttachment.ENCODING_HEX_BINARY)) {
                    return Hex.encodeHexString(binaryData).toUpperCase();
                } else {
                    throw new CitrusRuntimeException(String.format("Unsupported encoding type '%s' for SOAP attachment - choose one of %s or %s",
                            encodingType, SoapAttachment.ENCODING_BASE64_BINARY, SoapAttachment.ENCODING_HEX_BINARY));
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment data input stream", e);
            }
        }
    }

    /**
     * Set the content body.
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the content file resource path.
     * @return the content resource path
     */
    public String getContentResourcePath() {
        if (contentResourcePath != null) {
            if (context != null) {
                return context.replaceDynamicContentInString(contentResourcePath);
            } else {
                return contentResourcePath;
            }
        }

        return null;
    }

    /**
     * Set the content file resource path.
     * @param path the content resource path to set
     */
    public void setContentResourcePath(String path) {
        this.contentResourcePath = path;
    }

    /**
     * Get the charset name.
     * @return the charsetName
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * Set the charset name.
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    /**
     * Set the content type.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the content id.
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    /**
     * Set mtom inline
     * @param inline
     */
    public void setMtomInline(boolean inline) {
        this.mtomInline = inline;
    }

    /**
     * Get mtom inline
     * @return
     */
    public boolean isMtomInline() {
        return this.mtomInline;
    }

    /**
     * Gets the attachment encoding type.
     * @return
     */
    public String getEncodingType() {
        return encodingType;
    }

    /**
     * Sets the attachment encoding type.
     * @param encodingType
     */
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    /**
     * Sets the test context this attachment is bound to. Variable resolving takes place with this context instance.
     * @param context Test context used to resolve dynamic content
     */
    public void setTestContext(TestContext context) {
        this.context = context;
    }

    /**
     * Get size in bytes of the given input stream
     * @param is Read all data from stream to calculate size of the stream
     */
    private static long getSizeOfContent(InputStream is) throws IOException {
        long size = 0;
        while (is.read() != -1) {
            size++;
        }
        return size;
    }

    /**
     * Data source working on this attachments text content data.
     */
    private class ContentDataSource implements DataSource {
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(SoapAttachment.this.getContent().getBytes(charsetName));
        }

        @Override
        public String getContentType() {
            return SoapAttachment.this.getContentType();
        }

        @Override
        public String getName() {
            return SoapAttachment.this.getContentId();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Data source working on this attachments file resource.
     */
    private class FileResourceDataSource implements DataSource {

        @Override
        public InputStream getInputStream() throws IOException {
            return getFileResource().getInputStream();
        }

        @Override
        public String getContentType() {
            return SoapAttachment.this.getContentType();
        }

        @Override
        public String getName() {
            return FileUtils.getFileName(getFileResource().getLocation());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        private Resource getFileResource() {
            return Resources.create(SoapAttachment.this.getContentResourcePath());
        }
    }
}
