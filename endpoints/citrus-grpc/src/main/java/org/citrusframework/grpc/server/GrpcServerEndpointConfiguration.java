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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointConfiguration;

/**
 * Configuration of a gRPC server endpoint that simulates gRPC services for a client under test.
 */
public class GrpcServerEndpointConfiguration extends AbstractEndpointConfiguration {

    private int port;
    private List<String> services = new ArrayList<>();
    private List<String> descriptors = new ArrayList<>();
    private List<String> protoPaths = new ArrayList<>();
    private boolean tls;
    private String certificateChain;
    private String privateKey;
    private boolean omitDefaultValues;
    private long pollingInterval = 50L;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Full names of the services this simulator serves. An empty list serves every service found
     * in the loaded descriptors.
     */
    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<String> descriptors) {
        this.descriptors = descriptors;
    }

    public List<String> getProtoPaths() {
        return protoPaths;
    }

    public void setProtoPaths(List<String> protoPaths) {
        this.protoPaths = protoPaths;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getCertificateChain() {
        return certificateChain;
    }

    public void setCertificateChain(String certificateChain) {
        this.certificateChain = certificateChain;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isOmitDefaultValues() {
        return omitDefaultValues;
    }

    public void setOmitDefaultValues(boolean omitDefaultValues) {
        this.omitDefaultValues = omitDefaultValues;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
