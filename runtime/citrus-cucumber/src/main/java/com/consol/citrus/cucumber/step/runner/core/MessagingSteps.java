/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.cucumber.step.runner.core;

import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.cucumber.message.MessageCreators;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class MessagingSteps {

    @CitrusResource
    private DefaultTestCaseRunner runner;

    /** Available message creator POJO objects */
    private MessageCreators messageCreators;

    /** Messages defined by id */
    private Map<String, Message> messages;

    @Before
    public void before(Scenario scenario) {
        messageCreators = new MessageCreators();
        messages = new HashMap<>();
    }

    @Given("^message creator ([^\\s]+)$")
    public void messageCreator(String type) {
        messageCreators.addType(type);
    }

    @Given("^message ([^\\s]+)$")
    public void message(String messageId) {
        messages.put(messageId, new DefaultMessage());
    }

    @When("^<([^>]+)> sends message <([^>]+)>$")
    public void sendMessage(final String endpoint, final String messageId) {
        if (messages.containsKey(messageId)) {
            runner.when(send().endpoint(endpoint)
                    .message(new DefaultMessage(messages.get(messageId))));
        } else {
            runner.when(send().endpoint(endpoint)
                    .message(messageCreators.createMessage(messageId)));
        }
    }

    @Then("^<([^>]+)> should send message <([^>]+)>$")
    public void shouldSendMessage(String endpoint, String messageName) {
        sendMessage(endpoint, messageName);
    }

    @When("^<([^>]+)> sends$")
    public void doSendMessage(final String endpoint, final String payload) {
        runner.when(send().endpoint(endpoint)
                .payload(payload));
    }

    @When("^<([^>]+)> sends \"([^\"]*)\"$")
    public void sendPayload(String endpoint, String payload) {
        doSendMessage(endpoint, payload);
    }

    @Then("^<([^>]+)> should send \"([^\"]*)\"$")
    public void shouldSend(String endpoint, String payload) {
        doSendMessage(endpoint, payload);
    }

    @Then("^<([^>]+)> should send$")
    public void shouldSendPayload(String endpoint, String payload) {
        doSendMessage(endpoint, payload);
    }

    @When("^<([^>]+)> receives message <([^>]+)>$")
    public void receiveDefaultMessage(final String endpoint, final String messageName) {
        receiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, messageName);
    }

    @When("^<([^>]+)> receives ([^\\s]+) message <([^>]+)>$")
    public void receiveMessage(final String endpoint, final String type, final String messageId) {
        if (messages.containsKey(messageId)) {
            runner.when(receive().endpoint(endpoint)
                    .messageType(type)
                    .message(new DefaultMessage(messages.get(messageId))));
        } else {
            runner.when(receive().endpoint(endpoint)
                    .messageType(type)
                    .message(messageCreators.createMessage(messageId)));
        }
    }

    @Then("^<([^>]+)> should receive message <([^>]+)>$")
    public void shouldReceiveDefaultMessage(String endpoint, String messageName) {
        receiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, messageName);
    }

    @Then("^<([^>]+)> should receive ([^\\s]+) message <([^>]+)>$")
    public void shouldReceiveMessage(String endpoint, String type, String messageName) {
        receiveMessage(endpoint, type, messageName);
    }

    @When("^<([^>]+)> receives ([^\\s]+) \"([^\"]*)\"$")
    public void doReceiveMessage(final String endpoint, final String type, final String payload) {
        runner.when(receive().endpoint(endpoint)
        .messageType(type)
        .payload(payload));
    }

    @When("^<([^>]+)> receives \"([^\"]*)\"$")
    public void receiveDefault(String endpoint, String payload) {
        doReceiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, payload);
    }

    @When("^<([^>]+)> receives$")
    public void receiveDefaultPayload(String endpoint, String payload) {
        doReceiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, payload);
    }

    @When("^<([^>]+)> receives ([^\\s\"]+)$")
    public void receivePayload(String endpoint, String type, String payload) {
        doReceiveMessage(endpoint, type, payload);
    }

    @Then("^<([^>]+)> should receive ([^\\s]+) \"([^\"]*)\"$")
    public void shouldReceive(String endpoint, String type, String payload) {
        doReceiveMessage(endpoint, type, payload);
    }

    @Then("^<([^>]+)> should receive \"([^\"]*)\"$")
    public void shouldReceiveDefault(String endpoint, String payload) {
        doReceiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, payload);
    }

    @Then("^<([^>]+)> should receive$")
    public void shouldReceiveDefaultPayload(String endpoint, String payload) {
        doReceiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, payload);
    }

    @Then("^<([^>]+)> should receive ([^\\s\"]+)$")
    public void shouldReceivePayload(String endpoint, String type, String payload) {
        doReceiveMessage(endpoint, type, payload);
    }

    @And("^<([^>]+)> header ([^\\s]+)(?: is |=)\"([^\"]*)\"$")
    public void addHeader(String messageId, String name, String value) {
        if (!messages.containsKey(messageId)) {
            throw new CitrusRuntimeException(String.format("Unknown message '%s'", messageId));
        }

        messages.get(messageId).setHeader(name, value);
    }

    @And("^<([^>]+)> payload (?:is )?\"([^\"]*)\"$")
    public void addPayload(String messageId, String payload) {
        if (!messages.containsKey(messageId)) {
            throw new CitrusRuntimeException(String.format("Unknown message '%s'", messageId));
        }

        messages.get(messageId).setPayload(payload);
    }

    @And("^<([^>]+)> payload(?: is)?$")
    public void addPayloadMultiline(String messageId, String payload) {
        addPayload(messageId, payload);
    }
}
