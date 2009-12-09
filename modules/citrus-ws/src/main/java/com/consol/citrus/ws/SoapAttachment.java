/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws;

import java.io.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.springframework.ws.mime.Attachment;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SoapAttachment implements Attachment {
    private String content;
    
    private String contentType = "text/plain";
    
    private String contentId = null;
    
    private String charsetName = "UTF-8";
    
    public SoapAttachment() {
    }
    
    public SoapAttachment(String content) {
        this.content = content;
    }
    
    public String getContentId() {
        return contentId;
    }

    public String getContentType() {
        return contentType;
    }

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

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes(charsetName));
    }

    public long getSize() {
        try {
            return content.getBytes(charsetName).length;
        } catch (UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the charsetName
     */
    public String getCharsetName() {
        return charsetName;
    }

    /**
     * @param charsetName the charsetName to set
     */
    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
}