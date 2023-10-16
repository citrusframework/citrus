/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic header message validator provides message header validation. Subclasses only have to add
 * specific logic for message payload validation. This validator is based on a control message.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageHeaderValidator extends AbstractMessageValidator<HeaderValidationContext> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageHeaderValidator.class);

    /** List of special header validators */
    private List<HeaderValidator> validators = new ArrayList<>();

    /** Set of default header validators located via resource path lookup */
    private static final Map<String, HeaderValidator> DEFAULT_VALIDATORS = HeaderValidator.lookup();

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, HeaderValidationContext validationContext) {
        Map<String, Object> controlHeaders = controlMessage.getHeaders();
        Map<String, Object> receivedHeaders = receivedMessage.getHeaders();

        if (controlHeaders == null || controlHeaders.isEmpty()) {
            return;
        }

        logger.debug("Start message header validation ...");

        for (Map.Entry<String, Object> entry : controlHeaders.entrySet()) {
            if (MessageHeaderUtils.isSpringInternalHeader(entry.getKey()) ||
                    entry.getKey().startsWith(MessageHeaders.MESSAGE_PREFIX) ||
                    entry.getKey().equals(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME) ||
                    entry.getKey().equals(EndpointUriResolver.REQUEST_PATH_HEADER_NAME) ||
                    entry.getKey().equals(EndpointUriResolver.QUERY_PARAM_HEADER_NAME)) {
                continue;
            }

            final String headerName = getHeaderName(entry.getKey(), receivedHeaders, context, validationContext);

            if (!receivedHeaders.containsKey(headerName)) {
                throw new ValidationException("Validation failed: Header element '" + headerName + "' is missing");
            }

            Object controlValue = entry.getValue();
            validationContext.getValidators()
                    .stream()
                    .filter(validator -> validator.supports(headerName, Optional.ofNullable(controlValue).map(Object::getClass).orElse(null)))
                    .findFirst()
                    .orElseGet(() ->
                        validationContext.getValidatorNames()
                                .stream()
                                .map(beanName -> {
                                    try {
                                        return context.getReferenceResolver().resolve(beanName, HeaderValidator.class);
                                    } catch (CitrusRuntimeException e) {
                                        logger.warn("Failed to resolve header validator for name: " + beanName);
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .filter(validator -> validator.supports(headerName, Optional.ofNullable(controlValue).map(Object::getClass).orElse(null)))
                                .findFirst()
                                .orElseGet(() ->
                                    getHeaderValidators(context).stream()
                                            .filter(validator -> validator.supports(headerName, Optional.ofNullable(controlValue).map(Object::getClass).orElse(null)))
                                            .findFirst()
                                            .orElseGet(DefaultHeaderValidator::new)
                                )
                    ).validateHeader(headerName, receivedHeaders.get(headerName), controlValue, context, validationContext);
        }

        logger.info("Message header validation successful: All values OK");
    }

    /**
     * Combines header validators from multiple sources. First the manual added validators in this class are added. Then
     * validators coming from reference resolver and resource path lookup are added.
     *
     * At the end a distinct combination of all validators is returned.
     * @param context
     * @return
     */
    private List<HeaderValidator> getHeaderValidators(TestContext context) {
        List<HeaderValidator> allValidators = new ArrayList<>(validators);

        // add validators from resource path lookup
        Map<String, HeaderValidator> validatorMap = new HashMap<>(DEFAULT_VALIDATORS);

        // add validators in reference resolver
        validatorMap.putAll(context.getReferenceResolver().resolveAll(HeaderValidator.class));

        allValidators.addAll(validatorMap.values());

        return allValidators.stream()
                        .distinct()
                        .collect(Collectors.toList());
    }

    /**
     * Get header name from control message but also check if header expression is a variable or function. In addition to that find case insensitive header name in
     * received message when feature is activated.
     *
     * @param name
     * @param receivedHeaders
     * @param context
     * @param validationContext
     * @return
     */
    private String getHeaderName(String name, Map<String, Object> receivedHeaders, TestContext context, HeaderValidationContext validationContext) {
        String headerName = context.resolveDynamicValue(name);

        if (!receivedHeaders.containsKey(headerName) &&
                validationContext.isHeaderNameIgnoreCase()) {
            String key = headerName;

            logger.debug(String.format("Finding case insensitive header for key '%s'", key));

            headerName = receivedHeaders
                    .entrySet()
                    .parallelStream()
                    .filter(item -> item.getKey().equalsIgnoreCase(key))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Validation failed: No matching header for key '" + key + "'"));

            logger.info(String.format("Found matching case insensitive header name: %s", headerName));
        }

        return headerName;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return true;
    }

    @Override
    protected Class<HeaderValidationContext> getRequiredValidationContextType() {
        return HeaderValidationContext.class;
    }

    /**
     * Adds header validator.
     * @param validator
     */
    public void addHeaderValidator(HeaderValidator validator) {
        this.validators.add(validator);
    }

    /**
     * Gets the validators.
     *
     * @return
     */
    public List<HeaderValidator> getValidators() {
        return Collections.unmodifiableList(validators);
    }

    /**
     * Sets the validators.
     *
     * @param validators
     */
    public void setValidators(List<HeaderValidator> validators) {
        this.validators = validators;
    }
}
