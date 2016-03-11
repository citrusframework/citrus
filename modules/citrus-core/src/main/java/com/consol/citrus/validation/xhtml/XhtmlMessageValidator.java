/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.validation.xhtml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.w3c.tidy.Tidy;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * XHTML message validator using W3C jtidy to automatically convert HTML content to XHTML fixing most common
 * well-formed errors in HTML markup.
 * 
 * @author Christoph Deppisch
 */
public class XhtmlMessageValidator extends DomXmlMessageValidator implements InitializingBean {

    /** W3C Tidy implementation for parsing HTML as XHTML */
    private Tidy tidyInstance;
    
    /** Resource pointing to a custom tidy configuration file */
    private Resource tidyConfiguration;
    
    /** Message type identifier */
    private static final String XHTML_MESSAGE_TYPE = "xhtml";

    /** Search pattern for base w3 xhtml url */
    private static final String W3_XHTML1_URL = "http://www\\.w3\\.org/TR/xhtml1/DTD/";
    
    /** Typical xhtml doytype definition */
    private static final String XHTML_DOCTYPE_DEFINITION = "DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0";
    
    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
            TestContext context, XmlMessageValidationContext validationContext)
            throws ValidationException {
        
        String messagePayload = receivedMessage.getPayload(String.class);
        String xhtmlPayload;
        
        // check if we already have XHTML message content
        if (messagePayload.contains(XHTML_DOCTYPE_DEFINITION)) {
            xhtmlPayload = messagePayload;
        } else {
            StringWriter xhtmlWriter = new StringWriter();
            tidyInstance.parse(new StringReader(messagePayload), xhtmlWriter);
            
            xhtmlPayload = xhtmlWriter.toString();
            xhtmlPayload = xhtmlPayload.replaceFirst(W3_XHTML1_URL, "org/w3/xhtml/");
        }
        
        super.validateMessage(new DefaultMessage(xhtmlPayload, receivedMessage.getHeaders()),
                controlMessage, context, validationContext);
    }
    
    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(XHTML_MESSAGE_TYPE);
    }

    /**
     * Set default Tidy instance.
     */
    public void afterPropertiesSet() throws Exception {
        if (tidyInstance == null) {
            tidyInstance = new Tidy();
            tidyInstance.setXHTML(true);
            tidyInstance.setShowWarnings(false);
            tidyInstance.setQuiet(true);
            tidyInstance.setEscapeCdata(true);
            tidyInstance.setTidyMark(false);
            
            if (tidyConfiguration != null) {
                tidyInstance.setConfigurationFromFile(tidyConfiguration.getFile().getAbsolutePath());
            }
        }
    }

    /**
     * Gets the tidyInstance.
     * @return the tidyInstance
     */
    public Tidy getTidyInstance() {
        return tidyInstance;
    }

    /**
     * Sets the tidyInstance.
     * @param tidyInstance the tidyInstance to set
     */
    public void setTidyInstance(Tidy tidyInstance) {
        this.tidyInstance = tidyInstance;
    }

    /**
     * Gets the tidyConfiguration.
     * @return the tidyConfiguration
     */
    public Resource getTidyConfiguration() {
        return tidyConfiguration;
    }

    /**
     * Sets the tidyConfiguration.
     * @param tidyConfiguration the tidyConfiguration to set
     */
    public void setTidyConfiguration(Resource tidyConfiguration) {
        this.tidyConfiguration = tidyConfiguration;
    }
    
}
