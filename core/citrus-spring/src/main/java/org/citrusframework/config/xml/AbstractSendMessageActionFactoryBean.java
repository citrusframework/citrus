package org.citrusframework.config.xml;

import java.util.List;

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.message.builder.SendMessageBuilderSupport;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractSendMessageActionFactoryBean<T extends SendMessageAction, M extends SendMessageBuilderSupport<T, B, M>, B extends SendMessageAction.SendMessageActionBuilder<T, M, B>> extends AbstractTestActionFactoryBean<T, B> {

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
    public void setMessageBuilder(MessageBuilder messageBuilder) {
        getBuilder().message(messageBuilder);
    }

    /**
     * Sets schema validation enabled/disabled for this message.
     *
     * @param enabled
     * @return
     */
    public void setSchemaValidation(final boolean enabled) {
        getBuilder().getMessageBuilderSupport().schemaValidation(enabled);
    }

    /**
     * Sets explicit schema instance name to use for schema validation.
     *
     * @param schemaName
     * @return
     */
    public void setSchema(final String schemaName) {
        getBuilder().getMessageBuilderSupport().schema(schemaName);
    }

    /**
     * Sets explicit schema repository instance to use for validation.
     *
     * @param schemaRepository
     * @return
     */
    public void setSchemaRepository(final String schemaRepository) {
        getBuilder().getMessageBuilderSupport().schemaRepository(schemaRepository);
    }
    /**
     * The variable extractors for this message sending action.
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
        getBuilder().message().type(messageType);
    }

    /**
     * Sets the data dictionary.
     * @param dataDictionary
     */
    public void setDataDictionary(DataDictionary<?> dataDictionary) {
        getBuilder().message().dictionary(dataDictionary);
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
