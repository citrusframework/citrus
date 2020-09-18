package com.consol.citrus.config.xml;

import java.util.List;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractSendMessageActionFactoryBean<T extends SendMessageAction, B extends SendMessageAction.SendMessageActionBuilder<T, B>> extends AbstractTestActionFactoryBean<T, B> {

    /**
     * Sets the message endpoint.
     * @param endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        getBuilder().endpoint(endpoint);
    }

    /**
     * Sets the message builder implementation.
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(MessageContentBuilder messageBuilder) {
        getBuilder().messageBuilder(messageBuilder);
    }

    /**
     * The variable extractors for this message sending action.
     * @param variableExtractors the variableExtractors to set
     */
    public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
        variableExtractors.forEach(getBuilder()::extract);
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
     * Enables fork mode for this message sender.
     * @param fork the fork to set.
     */
    public void setForkMode(boolean fork) {
        getBuilder().fork(fork);
    }

    /**
     * Sets the expected message type for this receive action.
     * @param messageType the messageType to set
     */
    public void setMessageType(String messageType) {
        getBuilder().messageType(messageType);
    }

    /**
     * Sets the data dictionary.
     * @param dataDictionary
     */
    public void setDataDictionary(DataDictionary<?> dataDictionary) {
        getBuilder().dictionary(dataDictionary);
    }

    /**
     * Sets the endpoint uri.
     * @param endpointUri
     */
    public void setEndpointUri(String endpointUri) {
        getBuilder().endpoint(endpointUri);
    }

    /**
     * Provides the test action builder implementation.
     * @return the test action builder for this particular factory bean.
     */
    protected abstract B getBuilder();
}
