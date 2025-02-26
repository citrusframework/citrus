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

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAgentAction extends AbstractTestAction implements AgentAction {

    protected final String agentName;

    /** Logger */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractAgentAction(String name, Builder<?, ?> builder) {
        super("citrus-agent-" + name, builder);

        this.agentName = builder.agentName;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends AbstractAgentAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> {

        private String agentName = CitrusAgentSettings.getAgentName();

        public B agent(String agentName) {
            this.agentName = agentName;
            return self;
        }
    }
}
