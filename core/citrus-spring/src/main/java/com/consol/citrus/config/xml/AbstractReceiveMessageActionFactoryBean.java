package com.consol.citrus.config.xml;

import java.util.List;
import java.util.Map;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.DefaultMessageContentBuilder;
import com.consol.citrus.validation.callback.ValidationCallback;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractReceiveMessageActionFactoryBean<T extends ReceiveMessageAction, B extends ReceiveMessageAction.ReceiveMessageActionBuilder<T, B>> extends AbstractTestActionFactoryBean<T, B> {

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
        messageProcessors.forEach(getBuilder()::process);
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
     * Sets the expected message type for this receive action.
     *
     * @param messageType the messageType to set
     */
    public void setMessageType(String messageType) {
        getBuilder().messageType(messageType);
    }

    /**
     * Sets the validationCallback.
     *
     * @param validationCallback the validationCallback to set
     */
    public void setValidationCallback(ValidationCallback validationCallback) {
        getBuilder().validationCallback(validationCallback);
    }

    /**
     * Sets the data dictionary.
     *
     * @param dataDictionary
     */
    public void setDataDictionary(DataDictionary<?> dataDictionary) {
        getBuilder().dictionary(dataDictionary);
    }

    /**
     * Sets the message builder implementation.
     *
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(DefaultMessageContentBuilder messageBuilder) {
        getBuilder().message(messageBuilder);
    }

    /**
     * Provides the test action builder implementation.
     * @return the test action builder for this particular factory bean.
     */
    protected abstract B getBuilder();
}
