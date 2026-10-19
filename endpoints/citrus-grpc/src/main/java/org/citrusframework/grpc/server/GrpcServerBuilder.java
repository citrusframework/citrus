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
package org.citrusframework.grpc.server;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.base.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.EndpointAdapter;

/**
 * Builds a {@link GrpcServer} endpoint.
 */
@SchemaType(module = "citrus-grpc")
@XmlType(name = "", propOrder = {})
public class GrpcServerBuilder extends AbstractEndpointBuilder<GrpcServer> {

    private final GrpcServer endpoint = new GrpcServer(new GrpcServerEndpointConfiguration());

    private String certificateChain;
    private String privateKey;
    private String endpointAdapter;

    @Override
    protected GrpcServer getEndpoint() {
        return endpoint;
    }

    private GrpcServerEndpointConfiguration configuration() {
        return (GrpcServerEndpointConfiguration) endpoint.getEndpointConfiguration();
    }

    public GrpcServerBuilder port(int port) {
        configuration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The local port the simulator binds to.")
    @XmlAttribute(name = "port")
    public void setPort(int port) {
        port(port);
    }

    public GrpcServerBuilder services(String... services) {
        configuration().setServices(Arrays.asList(services));
        return this;
    }

    @SchemaProperty(description = "Full service names served by the simulator. Defaults to all services in the descriptors.")
    @XmlAttribute(name = "services")
    public void setServices(List<String> services) {
        configuration().setServices(services);
    }

    public GrpcServerBuilder descriptors(String... descriptors) {
        configuration().setDescriptors(Arrays.asList(descriptors));
        return this;
    }

    @SchemaProperty(description = "Protobuf descriptor sources (.proto, descriptor set or generated class name).")
    @XmlAttribute(name = "descriptors")
    public void setDescriptors(List<String> descriptors) {
        configuration().setDescriptors(descriptors);
    }

    public GrpcServerBuilder protoPaths(String... protoPaths) {
        configuration().setProtoPaths(Arrays.asList(protoPaths));
        return this;
    }

    @SchemaProperty(description = "Additional import paths for .proto descriptor sources.")
    @XmlAttribute(name = "proto-paths")
    public void setProtoPaths(List<String> protoPaths) {
        configuration().setProtoPaths(protoPaths);
    }

    public GrpcServerBuilder tls(boolean tls) {
        configuration().setTls(tls);
        return this;
    }

    @SchemaProperty(description = "Enables TLS on the simulator server.")
    @XmlAttribute(name = "tls")
    public void setTls(boolean tls) {
        tls(tls);
    }

    public GrpcServerBuilder certificateChain(String certificateChain) {
        this.certificateChain = certificateChain;
        return this;
    }

    @SchemaProperty(description = "Certificate chain resource used by the TLS server.")
    @XmlAttribute(name = "certificate-chain")
    public void setCertificateChain(String certificateChain) {
        certificateChain(certificateChain);
    }

    public GrpcServerBuilder privateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    @SchemaProperty(description = "Private key resource used by the TLS server.")
    @XmlAttribute(name = "private-key")
    public void setPrivateKey(String privateKey) {
        privateKey(privateKey);
    }

    public GrpcServerBuilder omitDefaultValues(boolean omitDefaultValues) {
        configuration().setOmitDefaultValues(omitDefaultValues);
        return this;
    }

    @SchemaProperty(description = "Omits default-valued fields when printing received messages as JSON.")
    @XmlAttribute(name = "omit-default-values")
    public void setOmitDefaultValues(boolean omitDefaultValues) {
        omitDefaultValues(omitDefaultValues);
    }

    public GrpcServerBuilder timeout(long timeout) {
        configuration().setTimeout(timeout);
        endpoint.setDefaultTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Idle timeout in milliseconds before the simulator answers INTERNAL.")
    @XmlAttribute(name = "timeout")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    public GrpcServerBuilder autoStart(boolean autoStart) {
        endpoint.setAutoStart(autoStart);
        return this;
    }

    @SchemaProperty(description = "Starts the simulator automatically.", defaultValue = "true")
    @XmlAttribute(name = "auto-start")
    public void setAutoStart(boolean autoStart) {
        autoStart(autoStart);
    }

    public GrpcServerBuilder endpointAdapter(String endpointAdapter) {
        this.endpointAdapter = endpointAdapter;
        return this;
    }

    @SchemaProperty(advanced = true, description = "Bean reference to a custom endpoint adapter.")
    @XmlAttribute(name = "endpoint-adapter")
    public void setEndpointAdapter(String endpointAdapter) {
        endpointAdapter(endpointAdapter);
    }

    @Override
    public GrpcServer build() {
        configuration().setCertificateChain(certificateChain);
        configuration().setPrivateKey(privateKey);

        if (referenceResolver != null && endpointAdapter != null) {
            endpoint.setEndpointAdapter(referenceResolver.resolve(endpointAdapter, EndpointAdapter.class));
        }

        return super.build();
    }
}
