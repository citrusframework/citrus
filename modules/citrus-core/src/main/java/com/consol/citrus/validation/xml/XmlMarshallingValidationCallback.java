/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.validation.xml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.callback.AbstractValidationCallback;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;

/**
 * Validation callback automatically unmarshalling message payload so we work with
 * Java objects for validation.
 *  
 * @author Christoph Deppisch
 */
public abstract class XmlMarshallingValidationCallback<T> extends AbstractValidationCallback<T> {

    /** Unmarshaller */
    private Unmarshaller unmarshaller;
    
    /**
     * Default constructor.
     */
    public XmlMarshallingValidationCallback() {
        super();
    }
    
    /**
     * Default constructor with unmarshaller.
     */
    public XmlMarshallingValidationCallback(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }
    
    @Override
    public void validate(Message message, TestContext context) {
        validate(unmarshalMessage(message), message.getHeaders(), context);
    }
    
    @SuppressWarnings("unchecked")
    private T unmarshalMessage(Message message) {
        if (unmarshaller == null) {
            Assert.notNull(applicationContext, "Marshalling validation callback requires marshaller instance " +
            		"or Spring application context with nested bean definition of type marshaller");
            
            unmarshaller = applicationContext.getBean(Unmarshaller.class);
        }
        
        try {
            return (T) unmarshaller.unmarshal(getPayloadSource(message.getPayload()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to unmarshal message payload", e);
        }
    }

    /**
     * Creates the payload source for unmarshalling.
     * @param payload
     * @return
     */
    private Source getPayloadSource(Object payload) {
        Source source = null;
        
        if (payload instanceof String) {
            source = new StringSource((String) payload);
        } else if (payload instanceof File) {
            source = new StreamSource((File) payload);
        } else if (payload instanceof Document) {
            source = new DOMSource((Document) payload);
        } else if (payload instanceof Source) {
            source = (Source) payload;
        }
        
        if (source == null) {
            throw new CitrusRuntimeException("Failed to create payload source for unmarshalling message");
        }
        
        return source;
    }
}
