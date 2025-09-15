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

package org.citrusframework.cucumber.steps.standard;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.cucumber.steps.standard.message.MessageCreator;
import org.citrusframework.cucumber.steps.standard.message.MessageCreators;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

public class MessagingSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusFramework
    private Citrus citrus;

    /** Available message creator POJO objects */
    private MessageCreators messageCreators;

    /** Messages defined by id */
    private Map<String, Message> messages;

    @Before
    public void before() {
        messageCreators = new MessageCreators();

        citrus.getCitrusContext().getReferenceResolver().resolveAll(MessageCreator.class)
                .forEach(messageCreators::add);

        messages = new HashMap<>();
    }

    @Given("^message creator type ([^\\s]+)$")
    public void messageCreator(String type) {
        messageCreators.addType(type);
    }

    @Given("^message creator types$")
    public void messageCreators(DataTable types) {
        types.asList().forEach(messageCreators::addType);
    }

    @Given("^(?:create|new) message ([^\\s]+)$")
    public void message(String messageId) {
        messages.put(messageId, new DefaultMessage());
    }

    @When("^endpoint ([^\\s]+) sends message \\$([^\\s]+)$")
    @Then("^endpoint ([^\\s]+) should send message \\$([^\\s]+)$")
    public void sendMessage(final String endpoint, final String messageId) {
        if (messages.containsKey(messageId)) {
            runner.when(send()
                    .endpoint(endpoint)
                    .message(new DefaultMessage(messages.get(messageId))));
        } else {
            Message message = messageCreators.createMessage(messageId);
            runner.when(send().endpoint(endpoint)
                    .message(message));
        }
    }

    @When("^endpoint ([^\\s]+) sends body ([\\w\\W]+)$")
    @Then("^endpoint ([^\\s]+) should send body ([\\w\\W]+)$")
    public void sendBody(final String endpoint, final String body) {
        runner.when(send()
                .endpoint(endpoint)
                .message()
                .body(body));
    }

    @When("^endpoint ([^\\s]+) sends body$")
    @Then("^endpoint ([^\\s]+) should send body$")
    public void sendMultilineBody(String endpoint, String body) {
        sendBody(endpoint, body);
    }

    @When("^endpoint ([^\\s]+) receives ([^\\s]+) message \\$([^\\s]+)$")
    @Then("^endpoint ([^\\s]+) should receive ([^\\s]+) message \\$([^\\s]+)$")
    public void receiveMessage(final String endpoint, final String type, final String messageId) {
        if (messages.containsKey(messageId)) {
            runner.when(receive()
                    .endpoint(endpoint)
                    .message(new DefaultMessage(messages.get(messageId))
                                    .setType(type)));
        } else {
            Message message = messageCreators.createMessage(messageId);
            runner.when(receive().endpoint(endpoint)
                    .message(message)
                    .type(type));
        }
    }

    @When("^endpoint ([^\\s]+) receives message \\$([^\\s]+)$")
    @Then("^endpoint ([^\\s]+) should receive message \\$([^\\s]+)$")
    public void receiveMessage(final String endpoint, final String messageName) {
        receiveMessage(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, messageName);
    }

    @When("^endpoint ([^\\s]+) receives ([^\\s]+) body ([\\w\\W]+)$")
    @Then("^endpoint ([^\\s]+) should receive ([^\\s]+) body ([\\w\\W]+)$")
    public void receiveBody(final String endpoint, final String type, final String body) {
        runner.when(receive()
                .endpoint(endpoint)
                .message()
                .type(type)
                .body(body));
    }

    @When("^endpoint ([^\\s]+) receives body ([\\w\\W]+)$")
    @Then("^endpoint ([^\\s]+) should receive body ([\\w\\W]+)$")
    public void receiveDefault(String endpoint, String body) {
        receiveBody(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, body);
    }

    @When("^endpoint ([^\\s]+) receives body$")
    @Then("^endpoint ([^\\s]+) should receive body$")
    public void receiveMultilineBody(String endpoint, String body) {
        receiveBody(endpoint, CitrusSettings.DEFAULT_MESSAGE_TYPE, body);
    }

    @When("^endpoint ([^\\s]+) receives ([^\\s]+) body$")
    @Then("^endpoint ([^\\s]+) should receive ([^\\s]+) body$")
    public void shouldReceiveMultiline(String endpoint, String type, String body) {
        receiveBody(endpoint, type, body);
    }

    @And("^\\$([^\\s]+) header ([^\\s]+)(?: is |=)\"([^\"]*)\"$")
    public void addHeader(String messageId, String name, String value) {
        messages.get(messageId).setHeader(name, value);
    }

    @And("^\\$([^\\s]+) has body ([\\w\\W]+)$")
    public void addBody(String messageId, String body) {
        messages.get(messageId).setPayload(body);
    }

    @And("^\\$([^\\s]+) has body$")
    public void addBodyMultiline(String messageId, String body) {
        addBody(messageId, body);
    }
}
