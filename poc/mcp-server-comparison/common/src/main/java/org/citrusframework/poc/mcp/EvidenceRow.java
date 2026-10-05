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

package org.citrusframework.poc.mcp;

import java.util.Objects;

/**
 * One structured evidence row: candidate + version + hosting mode + transport +
 * scenario + dependency lane + JDK, with outcome, commands, counts, timings and
 * source references. Every executable scenario starts as {@code NOT RUN}.
 */
public final class EvidenceRow {

    public enum Outcome {
        NOT_RUN,
        PASS,
        FAIL,
        BLOCKED
    }

    public enum Suitability {
        UNVERIFIED,
        MEETS,
        DOES_NOT_MEET
    }

    private final String candidate;
    private final String candidateVersion;
    private final String hostingMode;
    private final String transport;
    private final String scenario;
    private final String dependencyLane;
    private final String jdk;
    private Outcome outcome;
    private Suitability suitability = Suitability.UNVERIFIED;
    private String expected;
    private String observed;
    private String command;
    private String reportPath;
    private int testCount;
    private String timing;
    private String references;
    private String blocker;

    public EvidenceRow(String candidate, String candidateVersion, String hostingMode,
            String transport, String scenario, String dependencyLane, String jdk) {
        this.candidate = Objects.requireNonNull(candidate);
        this.candidateVersion = Objects.requireNonNull(candidateVersion);
        this.hostingMode = Objects.requireNonNull(hostingMode);
        this.transport = Objects.requireNonNull(transport);
        this.scenario = Objects.requireNonNull(scenario);
        this.dependencyLane = Objects.requireNonNull(dependencyLane);
        this.jdk = Objects.requireNonNull(jdk);
        this.outcome = Outcome.NOT_RUN;
    }

    public EvidenceRow pass(String observed, String command, String reportPath, int testCount, String timing) {
        this.outcome = Outcome.PASS;
        this.observed = observed;
        this.command = command;
        this.reportPath = reportPath;
        this.testCount = testCount;
        this.timing = timing;
        return this;
    }

    public EvidenceRow fail(String expected, String observed, String command, String reportPath) {
        this.outcome = Outcome.FAIL;
        this.expected = expected;
        this.observed = observed;
        this.command = command;
        this.reportPath = reportPath;
        return this;
    }

    public EvidenceRow blocked(String blocker) {
        this.outcome = Outcome.BLOCKED;
        this.blocker = blocker;
        return this;
    }

    public EvidenceRow suitability(Suitability suitability) {
        this.suitability = suitability;
        return this;
    }

    public String getCandidate() {
        return candidate;
    }

    public String getScenario() {
        return scenario;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public Suitability getSuitability() {
        return suitability;
    }

    public String getBlocker() {
        return blocker;
    }

    /** Markdown table row for the comparison report. */
    public String toMarkdownRow() {
        return "| " + candidate + " " + candidateVersion + " | " + hostingMode + " | " + transport
                + " | " + scenario + " | " + dependencyLane + " | " + jdk + " | " + outcome
                + " | " + (observed != null ? observed : (blocker != null ? blocker : "—"))
                + " | " + (command != null ? "`" + command + "`" : "—") + " |";
    }
}
