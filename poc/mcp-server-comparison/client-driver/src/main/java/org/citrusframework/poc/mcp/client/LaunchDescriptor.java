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

package org.citrusframework.poc.mcp.client;

import java.util.List;
import java.util.Objects;

/**
 * Launch descriptor for one driver run. Either an HTTP URI or a child argument
 * array (never a shell command string) selects the server under test.
 */
public final class LaunchDescriptor {

    private final String candidate;
    private final String transport;
    private final String scenarioId;
    private final String httpUri;
    private final List<String> childArgs;
    private final long timeoutSeconds;
    private final String resultFile;

    private LaunchDescriptor(Builder builder) {
        this.candidate = Objects.requireNonNull(builder.candidate, "candidate");
        this.transport = Objects.requireNonNull(builder.transport, "transport");
        this.scenarioId = Objects.requireNonNull(builder.scenarioId, "scenarioId");
        this.httpUri = builder.httpUri;
        this.childArgs = builder.childArgs == null ? null : List.copyOf(builder.childArgs);
        this.timeoutSeconds = builder.timeoutSeconds;
        this.resultFile = Objects.requireNonNull(builder.resultFile, "resultFile");

        boolean http = httpUri != null;
        boolean child = childArgs != null && !childArgs.isEmpty();
        if (http == child) {
            throw new IllegalArgumentException("exactly one of httpUri or childArgs required");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be positive");
        }
        if (child && childArgs.stream().anyMatch(arg -> arg.contains("&&") || arg.contains("|") || arg.contains(";"))) {
            throw new IllegalArgumentException("childArgs must be an argument array, not a shell string");
        }
    }

    public String getCandidate() {
        return candidate;
    }

    public String getTransport() {
        return transport;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getHttpUri() {
        return httpUri;
    }

    public List<String> getChildArgs() {
        return childArgs;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getResultFile() {
        return resultFile;
    }

    public boolean isHttp() {
        return httpUri != null;
    }

    public static Builder http(String candidate, String transport, String scenarioId,
            String httpUri, String resultFile) {
        return new Builder(candidate, transport, scenarioId).httpUri(httpUri).resultFile(resultFile);
    }

    public static Builder child(String candidate, String transport, String scenarioId,
            List<String> childArgs, String resultFile) {
        return new Builder(candidate, transport, scenarioId).childArgs(childArgs).resultFile(resultFile);
    }

    public static final class Builder {
        private final String candidate;
        private final String transport;
        private final String scenarioId;
        private String httpUri;
        private List<String> childArgs;
        private long timeoutSeconds = 120;
        private String resultFile;

        private Builder(String candidate, String transport, String scenarioId) {
            this.candidate = candidate;
            this.transport = transport;
            this.scenarioId = scenarioId;
        }

        public Builder httpUri(String httpUri) {
            this.httpUri = httpUri;
            return this;
        }

        public Builder childArgs(List<String> childArgs) {
            this.childArgs = childArgs;
            return this;
        }

        public Builder timeoutSeconds(long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder resultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }

        public LaunchDescriptor build() {
            return new LaunchDescriptor(this);
        }
    }
}
