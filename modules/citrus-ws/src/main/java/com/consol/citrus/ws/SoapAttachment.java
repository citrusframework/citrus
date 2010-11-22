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

package com.consol.citrus.ws;

import java.io.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.springframework.ws.mime.Attachment;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Citrus SOAP attachment implementation.
 * 
 * @author Christoph Deppisch
 */
public class SoapAttachment implements Attachment {
    /** Content body as string */
    private String content;
    
    /** Content type */
    private String contentType = "text/plain";
    
    /** Content identifier */
    private String contentId = null;
    
    /** Chosen charset of content body */
    private String charsetName = "UTF-8";
    
    /**
     * Default constructor
     */
    public SoapAttachment() {
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
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes(charsetName));
    }

    /**
     * @see org.springframework.ws.mime.Attachment#getSize()
     */
    public long getSize() {
        try {
            return content.getBytes(charsetName).length;
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        }
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
}