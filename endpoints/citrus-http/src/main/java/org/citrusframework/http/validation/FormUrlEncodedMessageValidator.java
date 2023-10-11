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

package org.citrusframework.http.validation;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.model.Control;
import org.citrusframework.http.model.FormData;
import org.citrusframework.http.model.FormMarshaller;
import org.citrusframework.http.model.ObjectFactory;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.xml.StringResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

/**
 * Validates x-www-form-urlencoded HTML form data content by marshalling form fields to Xml representation.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FormUrlEncodedMessageValidator implements MessageValidator<ValidationContext> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(FormUrlEncodedMessageValidator.class);

    /** Message type this validator is bound to */
    public static final String MESSAGE_TYPE = "x-www-form-urlencoded";

    /** Form data message marshaller */
    private FormMarshaller formMarshaller = new FormMarshaller();

    /** Xml message validator delegate */
    private MessageValidator<? extends ValidationContext> xmlMessageValidator;

    /** Should form name value pairs be decoded by default */
    private boolean autoDecode = true;

    public static final String DEFAULT_XML_MESSAGE_VALIDATOR = "defaultXmlMessageValidator";

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, List<ValidationContext> validationContexts) throws ValidationException {
        logger.info("Start " + MESSAGE_TYPE + " message validation");

        try {
            Message formMessage = new DefaultMessage(receivedMessage);
            StringResult result = new StringResult();
            formMarshaller.marshal(createFormData(receivedMessage), result);
            formMessage.setPayload(result.toString());

            getXmlMessageValidator(context).validateMessage(formMessage, controlMessage, context, prepareValidationContexts(validationContexts));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Failed to validate " + MESSAGE_TYPE + " message", e);
        }

        logger.info("Validation of " + MESSAGE_TYPE + " message finished successfully: All values OK");
    }

    /**
     * Looks for provided XML message validation context in given list of contexts. If no such XML message validation context is
     * present add a new context that applies for XML message validation.
     * @param validationContexts
     * @return
     */
    private List<ValidationContext> prepareValidationContexts(List<ValidationContext> validationContexts) {
        Optional<XmlMessageValidationContext> provided = validationContexts.stream()
                .filter(XmlMessageValidationContext.class::isInstance)
                .map(XmlMessageValidationContext.class::cast)
                .findFirst();

        if (!provided.isPresent()) {
            List<ValidationContext> enriched = new ArrayList<>(validationContexts);
            enriched.add(new XmlMessageValidationContext());
            return enriched;
        }

        return validationContexts;
    }

    /**
     * Find proper XML message validator. Uses several strategies to lookup default XML message validator. Caches found validator for
     * future usage once the lookup is done.
     * @param context
     * @return
     */
    private MessageValidator<? extends ValidationContext> getXmlMessageValidator(TestContext context) {
        if (xmlMessageValidator != null) {
            return xmlMessageValidator;
        }

        // try to find xml message validator in registry
        Optional<MessageValidator<? extends ValidationContext>> defaultMessageValidator = context.getMessageValidatorRegistry().findMessageValidator(DEFAULT_XML_MESSAGE_VALIDATOR);

        if (!defaultMessageValidator.isPresent()
                && context.getReferenceResolver().isResolvable(DEFAULT_XML_MESSAGE_VALIDATOR)) {
            defaultMessageValidator = Optional.of(context.getReferenceResolver().resolve(DEFAULT_XML_MESSAGE_VALIDATOR, MessageValidator.class));
        }

        if (!defaultMessageValidator.isPresent()) {
            // try to find xml message validator via resource path lookup
            defaultMessageValidator = MessageValidator.lookup("xml");
        }

        if (defaultMessageValidator.isPresent()) {
            xmlMessageValidator = defaultMessageValidator.get();
            return xmlMessageValidator;
        }

        throw new CitrusRuntimeException("Unable to locate proper XML message validator - please add validator to project");
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

        if (message.getPayload() instanceof MultiValueMap) {
            MultiValueMap<String, Object> formValueMap = message.getPayload(MultiValueMap.class);

            for (Map.Entry<String, List<Object>> entry : formValueMap.entrySet()) {
                Control control = new ObjectFactory().createControl();
                control.setName(entry.getKey());
                control.setValue(entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")));
                formData.addControl(control);
            }
        } else {
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
        }

        return formData;
    }

    /**
     * Gets the default encoding. If set by Citrus system property (citrus.file.encoding) use
     * this one otherwise use system default.
     * @return
     */
    private String getEncoding() {
        return System.getProperty(CitrusSettings.CITRUS_FILE_ENCODING_PROPERTY, System.getenv(CitrusSettings.CITRUS_FILE_ENCODING_ENV) != null ?
                System.getenv(CitrusSettings.CITRUS_FILE_ENCODING_ENV) : Charset.defaultCharset().displayName());
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
