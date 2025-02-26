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

package org.citrusframework.agent.connector.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.springframework.util.Assert;

public class AgentActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<AgentAction> {

    private AbstractAgentAction.Builder<? extends AgentAction, ?> delegate;

    private String agentName = CitrusAgentSettings.getAgentName();

    /**
     * Fluent API action building entry method used in Java DSL.
     * @return
     */
    public static AgentActionBuilder agent() {
        return new AgentActionBuilder();
    }

    public AgentActionBuilder name(String agentName) {
        this.agentName = agentName;
        return this;
    }

    public AgentConnectAction.Builder connect() {
        AgentConnectAction.Builder builder = new AgentConnectAction.Builder();
        this.delegate = builder;
        return builder;
    }

    public AgentRunAction.Builder run() {
        AgentRunAction.Builder builder = new AgentRunAction.Builder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public AgentAction build() {
        Assert.notNull(delegate, "Missing delegate action to build");
        delegate.agent(agentName);
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
