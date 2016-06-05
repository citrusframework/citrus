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

package com.consol.citrus.cucumber.designer.core;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.cucumber.message.MessageCreator;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import cucumber.api.java.en.*;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class MessagingSteps {

    @CitrusResource
    private TestDesigner designer;

    private List<Object> messageCreators = new ArrayList<>();

    private SendMessageBuilder sendMessageBuilder;
    private ReceiveMessageBuilder receiveMessageBuilder;

    @Given("^message creator ([^\\s]+)$")
    public void messageCreator(String type) {
        try {
            messageCreators.add(Class.forName(type).newInstance());
        } catch (ClassNotFoundException | IllegalAccessException e) {
            throw new CitrusRuntimeException("Unable to access message creator type: " + type);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException("Unable to create  message creator instance of type: " + type);
        }
    }

    @When("^<([^>]*)> sends message <([^>]*)>$")
    public void sendMessage(String endpoint, String messageName) {
        sendMessageBuilder = designer.send(endpoint)
                .message(createMessage(messageName));
        receiveMessageBuilder = null;
    }

    @Then("^<([^>]*)> should send message <([^>]*)>$")
    public void shouldSendMessage(String endpoint, String messageName) {
        sendMessage(endpoint, messageName);
    }

    @When("^<([^>]*)> sends$")
    public void send(String endpoint, String payload) {
        sendMessageBuilder = designer.send(endpoint)
                .payload(payload);
        receiveMessageBuilder = null;
    }

    @When("^<([^>]*)> sends \"([^\"]*)\"$")
    public void sendPayload(String endpoint, String payload) {
        send(endpoint, payload);
    }

    @Then("^<([^>]*)> should send \"([^\"]*)\"$")
    public void shouldSend(String endpoint, String payload) {
        send(endpoint, payload);
    }

    @Then("^<([^>]*)> should send$")
    public void shouldSendPayload(String endpoint, String payload) {
        send(endpoint, payload);
    }

    @When("^<([^>]*)> receives message <([^>]*)>$")
    public void receiveMessage(String endpoint, final String messageName) {
        receiveMessageBuilder = designer.receive(endpoint)
                .message(createMessage(messageName));
        sendMessageBuilder = null;
    }

    @Then("^<([^>]*)> should receive message <([^>]*)>$")
    public void shouldReceiveMessage(String endpoint, String messageName) {
        receiveMessage(endpoint, messageName);
    }

    @When("^<([^>]*)> receives ([^\\s]+) \"([^\"]*)\"$")
    public void receive(String endpoint, String type, String payload) {
        receiveMessageBuilder = designer.receive(endpoint)
                .messageType(type)
                .payload(payload);
        sendMessageBuilder = null;
    }

    @When("^<([^>]*)> receives \"([^\"]*)\"$")
    public void receiveXml(String endpoint, String payload) {
        receive(endpoint, MessageType.XML.name(), payload);
    }

    @When("^<([^>]*)> receives$")
    public void receiveXmlPayload(String endpoint, String payload) {
        receive(endpoint, MessageType.XML.name(), payload);
    }

    @When("^<([^>]*)> receives ([^\\s]+)$")
    public void receivePayload(String endpoint, String type, String payload) {
        receive(endpoint, type, payload);
    }

    @Then("^<([^>]*)> should receive ([^\\s]+) \"([^\"]*)\"$")
    public void shouldReceive(String endpoint, String type, String payload) {
        receive(endpoint, type, payload);
    }

    @Then("^<([^>]*)> should receive \"([^\"]*)\"$")
    public void shouldReceiveXml(String endpoint, String payload) {
        receive(endpoint, MessageType.XML.name(), payload);
    }

    @Then("^<([^>]*)> should receive$")
    public void shouldReceiveXmlPayload(String endpoint, String payload) {
        receive(endpoint, MessageType.XML.name(), payload);
    }

    @Then("^<([^>]*)> should receive ([^\\s]+)$")
    public void shouldReceivePayload(String endpoint, String type, String payload) {
        receive(endpoint, type, payload);
    }

    @And("^message header ([^\\s]+) is \"([^\"]*)\"$")
    public void header(String name, String value) {
        if (sendMessageBuilder != null) {
            sendMessageBuilder.header(name, value);
        }

        if (receiveMessageBuilder != null) {
            receiveMessageBuilder.header(name, value);
        }
    }

    @And("^message header ([^\\s]+) should be \"([^\"]*)\"$")
    public void headerShouldBe(String name, String value) {
        header(name, value);
    }

    /**
     * Create message by delegating message creation to known message creators that
     * are able to create message with given name.
     * @param messageName
     * @return
     */
    private Message createMessage(final String messageName) {
        final Message[] message = { null };
        for (final Object messageCreator : messageCreators) {
            ReflectionUtils.doWithMethods(messageCreator.getClass(), new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.getAnnotation(MessageCreator.class).value().equals(messageName)) {
                        try {
                            message[0] = (Message) method.invoke(messageCreator);
                        } catch (InvocationTargetException e) {
                            throw new CitrusRuntimeException("Unsupported message creator method: " + method.getName());
                        }
                    }
                }
            }, new ReflectionUtils.MethodFilter() {
                @Override
                public boolean matches(Method method) {
                    return method.getAnnotationsByType(MessageCreator.class).length > 0;
                }
            });
        }

        if (message[0] == null) {
            throw new CitrusRuntimeException("Unable to find message creator for message: " + messageName);
        }

        return message[0];
    }
}
