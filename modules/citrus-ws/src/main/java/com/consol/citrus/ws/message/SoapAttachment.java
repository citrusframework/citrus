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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import org.springframework.ws.mime.Attachment;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Citrus SOAP attachment implementation.
 * 
 * @author Christoph Deppisch
 */
public class SoapAttachment implements Attachment, Serializable {

    /** Serial */
    private static final long serialVersionUID = 6277464458242523954L;

    /** Content body as string */
    private String content = null;

    /** Content body as binary */
    private DataHandler binaryContent = null;
        
    /** Content body as file resource path  */
    private String contentResourcePath;

    /** Content type */
    private String contentType = "text/plain";
    
    /** Content identifier */
    private String contentId = null;
    
    /** Chosen charset of content body */
    private String charsetName = "UTF-8";
    
    /** send mtom attachments inline as hex or base64 coded */
    private Boolean mtomInline = false;
    
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

        if (attachment.getContentType().startsWith("text/")) {
            // String content
            try {
                soapAttachment.setContent(FileUtils.readToString(attachment.getInputStream()).trim());
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read SOAP attachment content", e);
            }
        } else {
            // Binary content
            soapAttachment.binaryContent = attachment.getDataHandler();
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
    
    /**
     * @see org.springframework.ws.mime.Attachment#getContentId()
     */
    public String getContentId() {
        return contentId;
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getContentType()
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getDataHandler()
     */
    public DataHandler getDataHandler() {
        if(content != null) {
            // Text content
            return new DataHandler(new DataSource() {
                public OutputStream getOutputStream() throws IOException {
                    throw new UnsupportedOperationException();
                }

                public String getName() {
                    return contentId;
                }

                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(content.getBytes(charsetName));
                }

                public String getContentType() {
                    return contentType;
                }
            });
        } else {
            // Binary content
            if (binaryContent == null) {
                final Resource attachmentResource = new PathMatchingResourcePatternResolver().getResource(contentResourcePath);
                binaryContent = new DataHandler(new DataSource() {

                    @Override
                    public OutputStream getOutputStream() throws IOException {
                        throw new UnsupportedOperationException();
                    }

                    public String getName() {
                        return attachmentResource.getFilename();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return attachmentResource.getInputStream();
                    }

                    @Override
                    public String getContentType() {
                        return contentType;
                    }
                });
            }
            return binaryContent;
        }
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return getDataHandler().getInputStream();
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getSize()
     */
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
        return String.format("%s [contentId: %s, contentType: %s, content: %s]", getClass().getSimpleName().toUpperCase(), contentId, contentType, content);
    }

    /**
     * Get the content body.
     * @return the content
     */
    public String getContent() {
        return content;
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
    public void setMtomInline(Boolean inline) {
        this.mtomInline = inline;
    }

    /**
     * Get mtom inline
     * @return
     */
    public Boolean getMtomInline() {
        return this.mtomInline;
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
}