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

package org.citrusframework.kafka.endpoint.builder;

import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.kafka.endpoint.KafkaEndpointBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;

@SchemaType(module = "citrus-kafka")
@XmlType(name = "", propOrder = {
        "asynchronous",
        "synchronous"
})
@XmlRootElement(name = "kafka")
public class KafkaEndpointsBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware {

    private KafkaEndpointBuilder delegate;
    private ReferenceResolver referenceResolver;

    private String name;
    private String server;
    private String bootstrapServers;
    private String topic;
    private Integer partition;
    private Boolean autoCommit;
    private Boolean useThreadSafeConsumer;
    private Integer autoCommitInterval;
    private String offsetReset;
    private String clientId;
    private String consumerGroup;
    private String messageConverter;
    private String headerMapper;
    private String keySerializer;
    private String valueSerializer;
    private String keyDeserializer;
    private String valueDeserializer;
    private Long timeout;

    @SchemaProperty(description = "Sets the synchronous Kafka endpoint.")
    @XmlElement
    public void setSynchronous(KafkaEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "Sets the Kafka endpoint.")
    @XmlElement
    public void setAsynchronous(KafkaEndpointBuilder builder) {
        this.delegate = builder;
    }

    @SchemaProperty(description = "The name of the endpoint.")
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    @SchemaProperty(description = "Sets the Kafka bootstrap server.")
    @XmlAttribute
    public void setServer(String server) {
        this.server = server;
    }

    @SchemaProperty(description = "Sets the Kafka bootstrap servers.")
    @XmlAttribute(name = "bootstrap-servers")
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @SchemaProperty(description = "Sets the Kafka topic.")
    @XmlAttribute
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @SchemaProperty(description = "Sets the Kafka topic partition.")
    @XmlAttribute
    public void setPartition(int partition) {
        this.partition = partition;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets auto commit handling for this endpoint.")
    @XmlAttribute(name = "auto-commit")
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets thread safe consumer option for this endpoint.")
    @XmlAttribute(name = "use-thread-safe-consumer")
    public void setUseThreadSafeConsumer(boolean enabled) {
        this.useThreadSafeConsumer = enabled;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the auto commit interval.")
    @XmlAttribute(name = "auto-commit-interval")
    public void setAutoCommitInterval(int autoCommitInterval) {
        this.autoCommitInterval = autoCommitInterval;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the offset reset.")
    @XmlAttribute(name = "offset-reset")
    public void setOffsetReset(String offsetReset) {
        this.offsetReset = offsetReset;
    }

    @SchemaProperty(
            advanced = true,
            description = "Sets the client id used to connect to the Kafka server.")
    @XmlAttribute(name = "client-id")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the consumer group used by the endpoint.")
    @XmlAttribute(name = "consumer-group")
    public void setConsumerGroup(String group) {
        this.consumerGroup = group;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    @XmlAttribute(name = "message-converter")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    @SchemaProperty(advanced = true, description = "Sets the Kafka header mapper.")
    @XmlAttribute(name = "header-mapper")
    public void setHeaderMapper(String headerMapper) {
        this.headerMapper = headerMapper;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:serialize") },
            description = "Sets the fully qualified key serializer type class name.")
    @XmlAttribute(name = "key-serializer")
    public void setKeySerializer(String serializerType) {
        this.keySerializer = serializerType;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:serialize") },
            description = "Sets the fully qualified value serializer type class name.")
    @XmlAttribute(name = "value-serializer")
    public void setValueSerializer(String serializerType) {
        this.valueSerializer = serializerType;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:deserialize") },
            description = "Sets the fully qualified key deserializer type class name.")
    @XmlAttribute(name = "key-deserializer")
    public void setKeyDeserializer(String serializerType) {
        this.keyDeserializer = serializerType;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:deserialize") },
            description = "Sets the fully qualified value deserializer type class name.")
    @XmlAttribute(name = "value-deserializer")
    public void setValueDeserializer(String serializerType) {
        this.valueDeserializer = serializerType;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:consume") },
            description = "Sets the Kafka consumer timeout.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public Endpoint build() {
        if (delegate == null) {
            delegate = new KafkaEndpointBuilder();
        }

        if (name != null) {
            delegate.setName(name);
        }
        if (server != null) {
            delegate.setServer(server);
        }
        if (bootstrapServers != null) {
            delegate.setBootstrapServers(bootstrapServers);
        }
        if (topic != null) {
            delegate.setTopic(topic);
        }
        if (partition != null) {
            delegate.setPartition(partition);
        }
        if (autoCommit != null) {
            delegate.setAutoCommit(autoCommit);
        }
        if (useThreadSafeConsumer != null) {
            delegate.setUseThreadSafeConsumer(useThreadSafeConsumer);
        }
        if (autoCommitInterval != null) {
            delegate.setAutoCommitInterval(autoCommitInterval);
        }
        if (offsetReset != null) {
            delegate.setOffsetReset(offsetReset);
        }
        if (clientId != null) {
            delegate.setClientId(clientId);
        }
        if (consumerGroup != null) {
            delegate.setConsumerGroup(consumerGroup);
        }
        if (messageConverter != null) {
            delegate.setMessageConverter(messageConverter);
        }
        if (headerMapper != null) {
            delegate.setHeaderMapper(headerMapper);
        }
        if (keySerializer != null) {
            delegate.setKeySerializer(keySerializer);
        }
        if (valueSerializer != null) {
            delegate.setValueSerializer(valueSerializer);
        }
        if (keyDeserializer != null) {
            delegate.setKeyDeserializer(keyDeserializer);
        }
        if (valueDeserializer != null) {
            delegate.setValueDeserializer(valueDeserializer);
        }
        if (timeout != null) {
            delegate.setTimeout(timeout);
        }

        if (referenceResolver != null && delegate instanceof ReferenceResolverAware resolverAware) {
            resolverAware.setReferenceResolver(referenceResolver);
        }

        return delegate.build();
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return delegate != null && delegate.supports(endpointType);
    }

    @Override
    @XmlTransient
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
