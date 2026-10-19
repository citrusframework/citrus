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
package org.citrusframework.grpc.client;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;

/**
 * Builds a {@link GrpcClient} endpoint.
 */
@SchemaType(module = "citrus-grpc")
@XmlType(name = "", propOrder = {})
public class GrpcClientBuilder extends AbstractEndpointBuilder<GrpcClient> {

    private final GrpcClient endpoint = new GrpcClient(new GrpcClientEndpointConfiguration());

    private String trustCertificate;
    private String correlator;

    @Override
    protected GrpcClient getEndpoint() {
        return endpoint;
    }

    private GrpcClientEndpointConfiguration configuration() {
        return endpoint.getEndpointConfiguration();
    }

    public GrpcClientBuilder target(String target) {
        configuration().setTarget(target);
        return this;
    }

    @SchemaProperty(description = "The gRPC service target, e.g. host:port.")
    @XmlAttribute(name = "target")
    public void setTarget(String target) {
        target(target);
    }

    public GrpcClientBuilder descriptors(String... descriptors) {
        configuration().setDescriptors(Arrays.asList(descriptors));
        return this;
    }

    @SchemaProperty(description = "Protobuf descriptor sources (.proto, descriptor set or generated class name).")
    @XmlAttribute(name = "descriptors")
    public void setDescriptors(List<String> descriptors) {
        configuration().setDescriptors(descriptors);
    }

    public GrpcClientBuilder protoPaths(String... protoPaths) {
        configuration().setProtoPaths(Arrays.asList(protoPaths));
        return this;
    }

    @SchemaProperty(description = "Additional import paths for .proto descriptor sources.")
    @XmlAttribute(name = "proto-paths")
    public void setProtoPaths(List<String> protoPaths) {
        configuration().setProtoPaths(protoPaths);
    }

    public GrpcClientBuilder deadline(Duration deadline) {
        configuration().setDeadline(deadline);
        return this;
    }

    @SchemaProperty(description = "Default deadline in milliseconds applied to every call.")
    @XmlAttribute(name = "deadline")
    public void setDeadline(long deadline) {
        configuration().setDeadline(Duration.ofMillis(deadline));
    }

    public GrpcClientBuilder tls(boolean tls) {
        configuration().setTls(tls);
        return this;
    }

    @SchemaProperty(description = "Enables TLS on the client channel.")
    @XmlAttribute(name = "tls")
    public void setTls(boolean tls) {
        tls(tls);
    }

    public GrpcClientBuilder trustCertificate(String trustCertificate) {
        this.trustCertificate = trustCertificate;
        return this;
    }

    @SchemaProperty(description = "Certificate resource used to trust the server certificate.")
    @XmlAttribute(name = "trust-certificate")
    public void setTrustCertificate(String trustCertificate) {
        trustCertificate(trustCertificate);
    }

    public GrpcClientBuilder omitDefaultValues(boolean omitDefaultValues) {
        configuration().setOmitDefaultValues(omitDefaultValues);
        return this;
    }

    @SchemaProperty(description = "Omits default-valued fields when printing received messages as JSON.")
    @XmlAttribute(name = "omit-default-values")
    public void setOmitDefaultValues(boolean omitDefaultValues) {
        omitDefaultValues(omitDefaultValues);
    }

    public GrpcClientBuilder maxBufferedMessages(int maxBufferedMessages) {
        configuration().setMaxBufferedMessages(maxBufferedMessages);
        return this;
    }

    @SchemaProperty(description = "Maximum number of buffered stream messages per call.")
    @XmlAttribute(name = "max-buffered-messages")
    public void setMaxBufferedMessages(int maxBufferedMessages) {
        maxBufferedMessages(maxBufferedMessages);
    }

    public GrpcClientBuilder timeout(long timeout) {
        configuration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Receive timeout in milliseconds.")
    @XmlAttribute(name = "timeout")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    public GrpcClientBuilder pollingInterval(long pollingInterval) {
        configuration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Polling interval in milliseconds.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(long pollingInterval) {
        pollingInterval(pollingInterval);
    }

    public GrpcClientBuilder correlator(String correlator) {
        this.correlator = correlator;
        return this;
    }

    @SchemaProperty(advanced = true, description = "Bean reference to a message correlator.")
    @XmlAttribute(name = "correlator")
    public void setCorrelator(String correlator) {
        correlator(correlator);
    }

    @Override
    public GrpcClient build() {
        if (referenceResolver != null) {
            if (trustCertificate != null) {
                configuration().setTrustCertificate(trustCertificate);
            }
            if (correlator != null) {
                configuration().setCorrelator(referenceResolver.resolve(correlator, org.citrusframework.message.MessageCorrelator.class));
            }
        } else {
            configuration().setTrustCertificate(trustCertificate);
        }

        return super.build();
    }
}
