/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.http.validation;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.http.model.*;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.DefaultMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringResult;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

/**
 * Validates x-www-form-urlencoded HTML form data content by marshalling form fields to Xml representation.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FormUrlEncodedMessageValidator extends DefaultMessageValidator {

    /** Message type this validator is bound to */
    public static final String MESSAGE_TYPE = "x-www-form-urlencoded";

    /** Form data message marshaller */
    private FormMarshaller formMarshaller = new FormMarshaller();

    /** Xml message validator delegate */
    public DomXmlMessageValidator xmlMessageValidator = new DomXmlMessageValidator();

    /** Should form name value pairs be decoded by default */
    public boolean autoDecode = true;

    @Override
    public void validateMessagePayload(Message receivedMessage, Message controlMessage,
                                       ValidationContext validationContext, TestContext context) throws ValidationException {
        log.info("Start " + MESSAGE_TYPE + " message validation");

        try {
            XmlMessageValidationContext xmlMessageValidationContext = new XmlMessageValidationContext();

            Message formMessage = new DefaultMessage(receivedMessage);
            StringResult result = new StringResult();
            formMarshaller.marshal(createFormData(receivedMessage), result);
            formMessage.setPayload(result.toString());

            xmlMessageValidator.validateMessagePayload(formMessage, controlMessage, xmlMessageValidationContext, context);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Failed to validate " + MESSAGE_TYPE + " message", e);
        }

        log.info("Validation of " + MESSAGE_TYPE + " message finished successfully: All values OK");
    }

    /**
     * Create form data model object from url encoded message payload.
     * @param message
     * @return
     */
    private FormData createFormData(Message message) {
        FormData formData = new ObjectFactory().createFormData();

        formData.setContentType(getFormContentType(message));
        formData.setAction(getFormAction(message));

        String rawFormData = message.getPayload(String.class);
        if (StringUtils.hasText(rawFormData)) {
            StringTokenizer tokenizer = new StringTokenizer(rawFormData, "&");
            while (tokenizer.hasMoreTokens()) {
                Control control = new ObjectFactory().createControl();
                String[] nameValuePair = tokenizer.nextToken().split("=");

                if (autoDecode) {
                    try {
                        control.setName(URLDecoder.decode(nameValuePair[0], getEncoding()));
                        control.setValue(URLDecoder.decode(nameValuePair[1], getEncoding()));
                    } catch (UnsupportedEncodingException e) {
                        throw new CitrusRuntimeException(String.format("Failed to decode form control value '%s=%s'", nameValuePair[0], nameValuePair[1]), e);
                    }
                } else {
                    control.setName(nameValuePair[0]);
                    control.setValue(nameValuePair[1]);
                }
                formData.addControl(control);
            }
        }

        return formData;
    }

    /**
     * Gets the default encoding. If set by Citrus system property (citrus.file.encoding) use
     * this one otherwise use system default.
     * @return
     */
    private String getEncoding() {
        return System.getProperty(Citrus.CITRUS_FILE_ENCODING, Charset.defaultCharset().displayName());
    }

    /**
     * Reads form action target from message headers.
     * @param message
     * @return
     */
    private String getFormAction(Message message) {
        return message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI) != null ? message.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI).toString() : null;
    }

    /**
     * Reads form content type from message headers.
     * @param message
     * @return
     */
    private String getFormContentType(Message message) {
        return message.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE) != null ? message.getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE).toString() : null;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return MESSAGE_TYPE.equalsIgnoreCase(messageType);
    }
}
