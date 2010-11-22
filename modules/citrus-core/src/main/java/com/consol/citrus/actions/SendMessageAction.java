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

package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.validation.builder.MessageContentBuilder;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.variable.VariableExtractor;


/**
 * This action sends a messages to a specified message endpoint. The action holds a reference to
 * a {@link MessageSender}, which is capable of the message transport implementation. So action is
 * independent of the message transport.
 *
 * @author Christoph Deppisch 
 * @since 2008
 */
public class SendMessageAction extends AbstractTestAction {
    /** The message sender */
    protected MessageSender messageSender;
    
    /** List of variable extractors responsible for creating variables from received message content */
    private List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
    
    /** Builder constructing a control message */
    private MessageContentBuilder<?> messageBuilder = new PayloadTemplateMessageBuilder();

    /**
     * Message is constructed with payload and header entries and sent via
     * {@link MessageSender} instance.
     * 
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        Message<?> message = createMessage(context);
        
        // extract variables from before sending message so we can save dynamic message ids
        for (VariableExtractor variableExtractor : variableExtractors) {
            variableExtractor.extractVariables(message, context);
        }
        
        messageSender.send(message);
    }

    /**
     * Create message to be sent.
     * @param context
     * @return
     */
    protected Message<?> createMessage(TestContext context) {
        return messageBuilder.buildMessageContent(context);
    }

    /**
     * Set the message sender instance.
     * @param messageSender the messageSender to set
     */
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Sets the message builder implementation.
     * @param messageBuilder the messageBuilder to set
     */
    public void setMessageBuilder(MessageContentBuilder<?> messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    /**
     * The variable extractors for this message sending action.
     * @param variableExtractors the variableExtractors to set
     */
    public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
        this.variableExtractors = variableExtractors;
    }

    /**
     * Get the variable extractors.
     * @return the variableExtractors
     */
    public List<VariableExtractor> getVariableExtractors() {
        return variableExtractors;
    }
}