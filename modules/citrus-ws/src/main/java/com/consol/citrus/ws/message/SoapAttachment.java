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

package com.consol.citrus.ws.message;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.springframework.ws.mime.Attachment;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.nio.charset.Charset;

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
    private String charsetName = "UTF-8";
    
    /** send mtom attachments inline as hex or base64 coded */
    private boolean mtomInline = false;
    
    /** Content data handler */
    private DataHandler dataHandler = null;
    
    /** Optional MTOM encoding */
    private String encodingType = ENCODING_BASE64_BINARY;

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
        soapAttachment.setContentId(attachment.getContentId());
        soapAttachment.setContentType(attachment.getContentType());

        if (attachment.getContentType().startsWith("text")) {
            try {
                soapAttachment.setContent(FileUtils.readToString(attachment.getInputStream()).trim());
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment content", e);
            }
        } else {
            // Binary content
            soapAttachment.setDataHandler(attachment.getDataHandler());
        }

        soapAttachment.setCharsetName(System.getProperty(CitrusConstants.CITRUS_FILE_ENCODING,
                Charset.defaultCharset().displayName()));

        return soapAttachment;
    }

    /**
     * Constructor using fields.
     * @param content
     */
    public SoapAttachment(String content) {
        this.content = content;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public DataHandler getDataHandler() {
        if (dataHandler == null) {
            if (StringUtils.hasText(contentResourcePath)) {
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
                return content.getBytes(charsetName).length;
            } else {
                return getSizeOfContent(getDataHandler().getInputStream());
            }
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException ioe) {
            throw new CitrusRuntimeException(ioe);
        }
    }

    @Override
    public String toString() {
        return String.format("%s [contentId: %s, contentType: %s, content: %s]", getClass().getSimpleName().toUpperCase(), contentId, contentType, getContent());
    }

    /**
     * Get the content body.
     * @return the content
     */
    public String getContent() {
        if (StringUtils.hasText(content)) {
            return content;
        } else if (StringUtils.hasText(contentResourcePath) && contentType.startsWith("text")) {
            try {
                return FileUtils.readToString(new PathMatchingResourcePatternResolver().getResource(contentResourcePath).getInputStream(), Charset.forName(charsetName));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment file resource", e);
            }
        } else {
            try {
                byte[] binaryData = FileUtils.readToString(getDataHandler().getInputStream(), Charset.forName(charsetName)).getBytes(Charset.forName(charsetName));
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
        return contentResourcePath;
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
     * Resolve dynamic string content in attachment
     * @param context Test context used to resolve dynamic content
     */
    public void resolveDynamicContent(TestContext context) {
        // handle variables in content id
        if (contentId != null) {
            contentId = context.replaceDynamicContentInString(contentId);
        }

        // handle variables in content type
        if (contentType != null) {
            contentType = context.replaceDynamicContentInString(contentType);
        }

        if (StringUtils.hasText(content)) {
            content = context.replaceDynamicContentInString(content);
        } else if (contentResourcePath != null) {
            contentResourcePath = context.replaceDynamicContentInString(contentResourcePath);

            if (contentType.startsWith("text")) {
                try {
                    content = context.replaceDynamicContentInString(FileUtils.readToString(new PathMatchingResourcePatternResolver().getResource(contentResourcePath).getInputStream(), Charset.forName(charsetName)));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read SOAP attachment file resource", e);
                }
            }
        }
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
            return contentType;
        }

        @Override
        public String getName() {
            return getFileResource().getFilename();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        private Resource getFileResource() {
            return new PathMatchingResourcePatternResolver().getResource(contentResourcePath);
        }
    }
}