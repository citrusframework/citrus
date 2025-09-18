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

package org.citrusframework.cucumber.steps.jms;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.ConnectionFactory;
import org.citrusframework.Citrus;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.steps.jms.connection.ConnectionFactoryCreator;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.endpoint.JmsEndpointBuilder;
import org.citrusframework.util.FileUtils;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

public class JmsSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @CitrusFramework
    private Citrus citrus;

    private Map<String, Object> headers = new HashMap<>();
    private String body;

    private JmsEndpoint jmsEndpoint;

    private ConnectionFactory connectionFactory;

    private String selector = "";

    private String endpointName = JmsSettings.getEndpointName();

    private long timeout = JmsSettings.getTimeout();

    @Before
    public void before(Scenario scenario) {
        if (jmsEndpoint == null) {
            if (citrus.getCitrusContext().getReferenceResolver().resolveAll(JmsEndpoint.class).size() == 1L) {
                jmsEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(JmsEndpoint.class);
            } else if (citrus.getCitrusContext().getReferenceResolver().isResolvable(endpointName)) {
                jmsEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(endpointName, JmsEndpoint.class);
            } else {
                jmsEndpoint = new JmsEndpointBuilder()
                        .timeout(timeout)
                        .build();
                citrus.getCitrusContext().getReferenceResolver().bind(endpointName, jmsEndpoint);
            }
        }

        if (connectionFactory == null
                && citrus.getCitrusContext().getReferenceResolver().resolveAll(ConnectionFactory.class).size() == 1L) {
            connectionFactory = citrus.getCitrusContext().getReferenceResolver().resolve(ConnectionFactory.class);

            if (jmsEndpoint.getEndpointConfiguration().getConnectionFactory() == null) {
                jmsEndpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);
            }
        }

        headers = new HashMap<>();
        body = null;
    }

    @Given("^(?:JMS|jms) connection factory ([^\\s]+)$")
    public void setConnectionFactory(String name) {
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(name)) {
            connectionFactory = citrus.getCitrusContext().getReferenceResolver().resolve(name, ConnectionFactory.class);
            jmsEndpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);
            jmsEndpoint.getEndpointConfiguration().getJmsTemplate().setConnectionFactory(connectionFactory);
        } else {
            throw new CitrusRuntimeException(String.format("Unable to find connection factory '%s'", name));
        }
    }

    @Given("^(?:JMS|jms) connection factory$")
    public void setConnection(DataTable properties) throws ClassNotFoundException {
        List<List<String>> cells = properties.cells();
        Map<String, String> connectionSettings = new LinkedHashMap<>();
        cells.forEach(row -> connectionSettings.put(row.get(0), context.replaceDynamicContentInString(row.get(1))));

        connectionFactory = ConnectionFactoryCreator.lookup(connectionSettings.get("type"))
                                                    .create(connectionSettings);

        citrus.getCitrusContext().getReferenceResolver().bind("connectionFactory", connectionFactory);
        jmsEndpoint.getEndpointConfiguration().setConnectionFactory(connectionFactory);
        jmsEndpoint.getEndpointConfiguration().getJmsTemplate().setConnectionFactory(connectionFactory);
    }

    @Given("^(?:JMS|jms) destination: ([^\\s]+)$")
    public void setDestination(String destination) {
        jmsEndpoint.getEndpointConfiguration().setDestinationName(destination);
        jmsEndpoint.getEndpointConfiguration().getJmsTemplate().setDefaultDestinationName(destination);
    }

    @Given("^(?:JMS|jms) endpoint \"([^\"\\s]+)\"$")
    public void setEndpoint(String name) {
        this.endpointName = name;
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(name)) {
            jmsEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(name, JmsEndpoint.class);
        } else if (jmsEndpoint != null) {
            citrus.getCitrusContext().getReferenceResolver().bind(endpointName, jmsEndpoint);
            jmsEndpoint.setName(endpointName);
        }
    }

    @Given("^(?:JMS|jms) selector: (.+)$")
    public void selector(String selector) {
        this.selector = selector;
    }

    @Given("^(?:JMS|jms) consumer timeout is (\\d+)(?: ms| milliseconds)$")
    public void configureTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Given("^(?:JMS|jms) message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    @Then("^(?:expect|verify) (?:JMS|jms) message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addMessageHeader(String name, Object value) {
        headers.put(name, value);
    }

    @Given("^(?:JMS|jms) message headers$")
    public void addMessageHeaders(DataTable headers) {
        Map<String, Object> headerPairs = headers.asMap(String.class, Object.class);
        headerPairs.forEach(this::addMessageHeader);
    }

    @Given("^(?:JMS|jms) message body$")
    @Then("^(?:expect|verify) (?:JMS|jms) message body$")
    public void setMessageBodyMultiline(String body) {
        setMessageBody(body);
    }

    @Given("^load (?:JMS|jms) message body ([^\\s]+)$")
    @Given("^(?:expect|verify) (?:JMS|jms) message body loaded from ([^\\s]+)$")
    public void loadMessageBody(String file) {
        try {
            setMessageBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Given("^(?:JMS|jms) message body: (.+)$")
    @Then("^(?:expect|verify) (?:JMS|jms) message body: (.+)$")
    public void setMessageBody(String body) {
        this.body = body;
    }

    @When("^send (?:JMS|jms) message with body: (.+)$")
    @Given("^(?:JMS|jms) message with body: (.+)$")
    public void sendMessageBody(String body) {
        setMessageBody(body);
        sendMessage();
    }

    @When("^send (?:JMS|jms) message with body$")
    @Given("^(?:JMS|jms) message with body$")
    public void sendMessageBodyMultiline(String body) {
        sendMessageBody(body);
    }

    @When("^send (?:JMS|jms) message with body and headers: (.+)$")
    @Given("^(?:JMS|jms) message with body and headers: (.+)$")
    public void sendMessageBodyAndHeaders(String body, DataTable headers) {
        setMessageBody(body);
        addMessageHeaders(headers);
        sendMessage();
    }

    @Then("^(?:receive|expect|verify) (?:JMS|jms) message with body: (.+)$")
    public void receiveMessageBody(String body) {
        setMessageBody(body);
        receiveMessage();
    }

    @Then("^(?:receive|expect|verify) (?:JMS|jms) message with body$")
    public void receiveMessageBodyMultiline(String body) {
        receiveMessageBody(body);
    }

    @Then("^(?:receive|expect|verify) (?:JMS|jms) message with body and headers: (.+)$")
    public void receiveFromJms(String body, DataTable headers) {
        setMessageBody(body);
        addMessageHeaders(headers);
        receiveMessage();
    }

    @When("^send (?:JMS|jms) message$")
    public void sendMessage() {
        runner.run(send().endpoint(jmsEndpoint)
                .message()
                .body(body)
                .headers(headers));

        body = null;
        headers.clear();
    }

    @Then("^receive (?:JMS|jms) message$")
    public void receiveMessage() {
        runner.run(receive().endpoint(jmsEndpoint)
                .selector(selector)
                .timeout(timeout)
                .message()
                .body(body)
                .headers(headers));

        body = null;
        headers.clear();
    }

    @When("^send (?:JMS|jms) message to destination (.+)$")
    public void sendMessage(String destination) {
        setDestination(destination);
        sendMessage();
    }

    @Then("^receive (?:JMS|jms) message on destination (.+)")
    public void receiveMessage(String destination) {
        setDestination(destination);
        receiveMessage();
    }
}
