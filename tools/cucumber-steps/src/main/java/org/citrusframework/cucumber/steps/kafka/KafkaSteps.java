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

package org.citrusframework.cucumber.steps.kafka;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.cucumber.util.ResourceUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.endpoint.KafkaEndpointBuilder;
import org.citrusframework.kafka.message.KafkaMessage;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;

public class KafkaSteps {

    @CitrusResource
    private TestCaseRunner runner;

    @CitrusResource
    private TestContext context;

    @CitrusFramework
    private Citrus citrus;

    private Map<String, Object> headers = new HashMap<>();
    private String body;

    private KafkaEndpoint kafkaEndpoint;

    private String messageKey;
    private Integer partition;
    private String topic = "test";

    private String endpointName = KafkaSettings.getEndpointName();

    private long timeout = KafkaSettings.getConsumerTimeout();

    private String messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;

    @Before
    public void before(Scenario scenario) {
        if (kafkaEndpoint == null) {
            if (citrus.getCitrusContext().getReferenceResolver().resolveAll(KafkaEndpoint.class).size() == 1L) {
                kafkaEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(KafkaEndpoint.class);
            } else if (citrus.getCitrusContext().getReferenceResolver().isResolvable(endpointName)) {
                kafkaEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(endpointName, KafkaEndpoint.class);
            } else {
                kafkaEndpoint = new KafkaEndpointBuilder().build();
                citrus.getCitrusContext().getReferenceResolver().bind(endpointName, kafkaEndpoint);
            }
        }

        headers = new HashMap<>();
        body = null;

        messageType = CitrusSettings.DEFAULT_MESSAGE_TYPE;
        messageKey = null;
        partition = null;
    }

    @Given("^(?:Kafka|kafka) connection$")
    public void setConnection(DataTable properties) {
        Map<String, String> connectionProps = properties.asMap(String.class, String.class);

        String url = connectionProps.getOrDefault("url", "localhost:9092");
        String topicName = connectionProps.getOrDefault("topic", this.topic);
        String consumerGroup = connectionProps.getOrDefault("consumerGroup", KafkaMessageHeaders.KAFKA_PREFIX + "group");
        String offsetReset = connectionProps.getOrDefault("offsetReset", "earliest");

        setTopic(context.replaceDynamicContentInString(topicName));
        kafkaEndpoint.getEndpointConfiguration().setServer(context.replaceDynamicContentInString(url));
        kafkaEndpoint.getEndpointConfiguration().setOffsetReset(context.replaceDynamicContentInString(offsetReset));
        kafkaEndpoint.getEndpointConfiguration().setConsumerGroup(context.replaceDynamicContentInString(consumerGroup));
    }

    @Given("^new (?:Kafka|kafka) connection$")
    public void createConnection(DataTable properties) {
        setConnection(properties);
        kafkaEndpoint = new KafkaEndpoint(kafkaEndpoint.getEndpointConfiguration());
        citrus.getCitrusContext().getReferenceResolver().bind(endpointName, kafkaEndpoint);
    }

    @Given("^(?:Kafka|kafka) producer configuration$")
    public void setProducerConfig(DataTable properties) {
        Map<String, Object> producerProperties = properties.asMap(String.class, Object.class);
        kafkaEndpoint.getEndpointConfiguration().setProducerProperties(producerProperties);
    }

    @Given("^(?:Kafka|kafka) consumer configuration$")
    public void setConsumerConfig(DataTable properties) {
        Map<String, Object> consumerProperties = properties.asMap(String.class, Object.class);
        kafkaEndpoint.getEndpointConfiguration().setConsumerProperties(consumerProperties);
    }

    @Given("^(?:Kafka|kafka) endpoint \"([^\"\\s]+)\"$")
    public void setServer(String name) {
        this.endpointName = name;
        if (citrus.getCitrusContext().getReferenceResolver().isResolvable(name)) {
            kafkaEndpoint = citrus.getCitrusContext().getReferenceResolver().resolve(name, KafkaEndpoint.class);
        } else if (kafkaEndpoint != null) {
            citrus.getCitrusContext().getReferenceResolver().bind(endpointName, kafkaEndpoint);
            kafkaEndpoint.setName(endpointName);
        }
    }

    @Given("^(?:Kafka|kafka) message key: (.+)$")
    public void setMessageKey(String key) {
        this.messageKey = key;
    }

    @Given("^(?:Kafka|kafka) consumer timeout is (\\d+)(?: ms| milliseconds)$")
    public void setConsumerTimeout(int milliseconds) {
        this.timeout = milliseconds;
    }

    @Given("^(?:Kafka|kafka) topic partition: (\\d+)$")
    public void setPartition(int partition) {
        this.partition = partition;
    }

    @Given("^(?:Kafka|kafka) topic: (.+)$")
    public void setTopic(String topicName) {
        this.topic = topicName;
        kafkaEndpoint.getEndpointConfiguration().setTopic(topicName);
    }

    @Given("^(?:Kafka|kafka) message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    @Then("^(?:expect|verify) (?:Kafka|kafka) message header ([^\\s]+)(?:=| is )\"(.+)\"$")
    public void addMessageHeader(String name, Object value) {
        headers.put(name, value);
    }

    @Given("^(?:Kafka|kafka) message type ([^\\s]+)")
    public void setMessageType(String type) {
        this.messageType = type.toUpperCase();
    }

    @Given("^(?:Kafka|kafka) message headers$")
    public void addMessageHeaders(DataTable headers) {
        Map<String, Object> headerPairs = headers.asMap(String.class, Object.class);
        headerPairs.forEach(this::addMessageHeader);
    }

    @Given("^(?:Kafka|kafka) message body$")
    @Then("^(?:expect|verify) (?:Kafka|kafka) message body$")
    public void setMessageBodyMultiline(String body) {
        setMessageBody(body);
    }

    @Given("^load (?:Kafka|kafka) message body ([^\\s]+)$")
    @Given("^(?:expect|verify) (?:Kafka|kafka) message body loaded from ([^\\s]+)$")
    public void loadMessageBody(String file) {
        try {
            setMessageBody(FileUtils.readToString(ResourceUtils.resolve(file, context)));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to load body from file resource %s", file));
        }
    }

    @Given("^(?:Kafka|kafka) message body: (.+)$")
    @Then("^(?:expect|verify) (?:Kafka|kafka) message body: (.+)$")
    public void setMessageBody(String body) {
        this.body = body;
    }

    @When("^send (?:Kafka|kafka) message$")
    public void sendMessage() {
        runner.run(send().endpoint(kafkaEndpoint)
                .message(createKafkaMessage()));

        body = null;
        headers.clear();
    }

    @Then("^receive (?:Kafka|kafka) message$")
    public void receiveMessage() {
        runner.run(receive().endpoint(kafkaEndpoint)
                .timeout(timeout)
                .message(createKafkaMessage()));

        body = null;
        headers.clear();
    }

    @When("^send (?:Kafka|kafka) message to topic (.+)$")
    public void sendMessage(String topicName) {
        setTopic(topicName);
        sendMessage();
    }

    @Then("^receive (?:Kafka|kafka) message on topic (.+)")
    public void receiveMessage(String topicName) {
        setTopic(topicName);
        receiveMessage();
    }

    @When("^send (?:Kafka|kafka) message with body and headers: (.+)$")
    @Given("^message in (?:Kafka|kafka) with body and headers: (.+)$")
    public void sendMessageBodyAndHeaders(String body, DataTable headers) {
        setMessageBody(body);
        addMessageHeaders(headers);
        sendMessage();
    }

    @When("^send (?:Kafka|kafka) message with body: (.+)$")
    @Given("^message in (?:Kafka|kafka) with body: (.+)$")
    public void sendMessageBody(String body) {
        setMessageBody(body);
        sendMessage();
    }

    @When("^send (?:Kafka|kafka) message with body$")
    @Given("^message in (?:Kafka|kafka) with body$")
    public void sendMessageBodyMultiline(String body) {
        sendMessageBody(body);
    }

    @Then("^(?:receive|expect|verify) (?:Kafka|kafka) message with body and headers: (.+)$")
    public void receiveFromKafka(String body, DataTable headers) {
        setMessageBody(body);
        addMessageHeaders(headers);
        receiveMessage();
    }

    @Then("^(?:receive|expect|verify) (?:Kafka|kafka) message with body: (.+)$")
    public void receiveMessageBody(String body) {
        setMessageBody(body);
        receiveMessage();
    }

    @Then("^(?:receive|expect|verify) (?:Kafka|kafka) message with body$")
    public void receiveMessageBodyMultiline(String body) {
        receiveMessageBody(body);
    }

    private Message createKafkaMessage() {
        KafkaMessage message = new KafkaMessage(body, headers)
                .topic(topic);

        message.setType(messageType);

        if (messageKey != null) {
            message.messageKey(messageKey);
        }

        if (partition != null) {
            message.partition(partition);
        }
        return message;
    }

}
