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

package com.consol.citrus.ws.interceptor;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.xml.transform.TransformerObjectSupport;

import com.consol.citrus.util.XMLUtils;

/**
 * @author Christoph Deppisch
 */
public abstract class LoggingInterceptorSupport extends TransformerObjectSupport {

    /**
     * Logger
     */
    protected transient final Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Prevent instantiation. 
     */
    protected LoggingInterceptorSupport() {
    }
    
    /**
     * Log SOAP message with transformer instance.
     * 
     * @param logMessage the customized log message.
     * @param messageSource the message content as SOAP envelope source.
     * @throws TransformerException
     */
    protected void logSoapMessage(String logMessage, Source messageSource) throws TransformerException {
        Transformer transformer = createIndentingTransformer();
        StringWriter writer = new StringWriter();
        
        transformer.transform(messageSource, new StreamResult(writer));
        logMessage(logMessage + XMLUtils.prettyPrint(writer.toString()));
    }
    
    /**
     * Log WebService message (other than SOAP) with in memory
     * {@link ByteArrayOutputStream}
     * 
     * @param logMessage the customized log message.
     * @param message the message to log.
     */
    protected void logWebServiceMessage(String logMessage, WebServiceMessage message) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {
            message.writeTo(os);
            log.debug(logMessage + os.toString());
        } catch (IOException e) {
            log.warn("Unable to write WebService message to logger", e);
        }
    }
    
    /**
     * Performs the final logger call with dynamic message.
     * 
     * @param message the constructed log message.
     */
    private void logMessage(String message) {
        log.debug(message);
    }
    
    /**
     * Get transformer implementation with output properties set.
     * 
     * @return the transformer instance.
     * @throws TransformerConfigurationException
     */
    private Transformer createIndentingTransformer() throws TransformerConfigurationException {
        Transformer transformer = createTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        return transformer;
    }
}
