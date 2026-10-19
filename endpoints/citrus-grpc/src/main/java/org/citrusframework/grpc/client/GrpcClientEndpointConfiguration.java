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
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.citrusframework.endpoint.PollableEndpointConfiguration;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;

/**
 * Configuration of a {@link GrpcClient} endpoint.
 *
 * @since 5.1
 */
public class GrpcClientEndpointConfiguration extends AbstractEndpointConfiguration implements PollableEndpointConfiguration {

    private String target;
    private List<String> descriptors = new ArrayList<>();
    private List<String> protoPaths = new ArrayList<>();
    private Duration deadline;
    private boolean tls;
    private String trustCertificate;
    private boolean omitDefaultValues;
    private int maxBufferedMessages = 10_000;
    private long pollingInterval = 500L;

    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public Duration getDeadline() {
        return deadline;
    }

    public void setDeadline(Duration deadline) {
        this.deadline = deadline;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getTrustCertificate() {
        return trustCertificate;
    }

    public void setTrustCertificate(String trustCertificate) {
        this.trustCertificate = trustCertificate;
    }

    public boolean isOmitDefaultValues() {
        return omitDefaultValues;
    }

    public void setOmitDefaultValues(boolean omitDefaultValues) {
        this.omitDefaultValues = omitDefaultValues;
    }

    public int getMaxBufferedMessages() {
        return maxBufferedMessages;
    }

    public void setMaxBufferedMessages(int maxBufferedMessages) {
        this.maxBufferedMessages = maxBufferedMessages;
    }

    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    @Override
    public long getPollingInterval() {
        return pollingInterval;
    }

    @Override
    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
}
