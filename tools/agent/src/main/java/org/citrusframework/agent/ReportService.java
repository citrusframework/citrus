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

package org.citrusframework.agent;

import java.util.Optional;

import org.citrusframework.TestResult;
import org.citrusframework.agent.listener.AgentTestListener;
import org.citrusframework.report.TestResults;

public class ReportService {

    AgentTestListener agentTestListener;

    public Optional<TestResult> findByName(String name) {
        return agentTestListener.getResults().asList().stream().filter(r -> r.getTestName().equals(name)).findFirst();
    }

    public TestResults getResults() {
        return agentTestListener.getResults();
    }

    public void clearResults() {
        agentTestListener.reset();
    }

}
