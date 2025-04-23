/*
 * Copyright the original author or authors.
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

package org.citrusframework.config.xml;

import java.util.List;
import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;

public abstract class AbstractReceiveMessageActionFactoryBean<T extends ReceiveMessageAction, M extends ReceiveMessageBuilderSupport<T, B, M>, B extends ReceiveMessageAction.ReceiveMessageActionBuilder<T, M, B>> extends AbstractTestActionFactoryBean<T, B> {

    /**
     * Setter for messageSelectorMap.
     *
     * @param messageSelectorMap
     */
    public void setMessageSelectorMap(Map<String, String> messageSelectorMap) {
        getBuilder().selector(messageSelectorMap);
    }

    /**
     * Set message selector string.
     *
     * @param messageSelector
     */
    public void setMessageSelector(String messageSelector) {
        getBuilder().selector(messageSelector);
    }

    /**
     * Set message endpoint instance.
     *
     * @param endpoint the message endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        getBuilder().endpoint(endpoint);
    }

    /**
     * Sets the endpoint uri.
     *
     * @param endpointUri
     */
    public void setEndpointUri(String endpointUri) {
        getBuilder().endpoint(endpointUri);
    }

    /**
     * Set list of message validators.
     *
     * @param validators the message validators to set
     */
    public void setValidators(List<MessageValidator<? extends ValidationContext>> validators) {
        validators.forEach(getBuilder()::validator);
    }

    /**
     * Set the receive timeout.
     *
     * @param receiveTimeout the receiveTimeout to set
     */
    public void setReceiveTimeout(long receiveTimeout) {
        getBuilder().timeout(receiveTimeout);
    }

    /**
     * Set the list of variable extractors.
     *
     * @param variableExtractors the variableExtractors to set
     */
    public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
        variableExtractors.forEach(getBuilder()::process);
    }

    /**
     * Set the list of message processors.
     *
     * @param messageProcessors the messageProcessors to set
     */
    public void setMessageProcessors(List<MessageProcessor> messageProcessors) {
        messageProcessors.forEach(getBuilder().message()::process);
    }

    /**
     * Sets the list of available validation contexts for this action.
     *
     * @param validationContexts the validationContexts to set
     */
    public void setValidationContexts(List<ValidationContext> validationContexts) {
        validationContexts.forEach(getBuilder()::validate);
    }

    /**
     * Sets the list of available validation contexts for this action.
     *
     * @param validationContexts the validationContexts to set
     */
    public void setValidationContextBuilder(List<ValidationContext.Builder<?, ?>> validationContexts) {
        validationContexts.forEach(getBuilder()::validate);
    }

    /**
     * Sets the expected message type for this receive action.
     *
     * @param messageType the messageType to set
     */
    public void setMessageType(String messageType) {
        getBuilder().message().type(messageType);
    }

    /**
     * Sets the processor.
     *
     * @param processor the processor to set
     */
    public void setValidationProcessor(ValidationProcessor processor) {
        getBuilder().validate(processor);
    }

    /**
     * Sets the data dictionary.
     *
     * @param dataDictionary
     */
    public void setDataDictionary(DataDictionary<?> dataDictionary) {
        getBuilder().message().dictionary(dataDictionary);
    }

    /**
     * Sets the message builder implementation.
     *
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(DefaultMessageBuilder messageBuilder) {
        getBuilder().message().from(messageBuilder);
    }

    /**
     * Provides the test action builder implementation.
     * @return the test action builder for this particular factory bean.
     */
    protected abstract B getBuilder();
}
