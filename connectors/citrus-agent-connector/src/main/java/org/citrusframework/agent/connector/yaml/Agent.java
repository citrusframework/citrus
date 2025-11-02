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

package org.citrusframework.agent.connector.yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.citrusframework.agent.connector.actions.AbstractAgentAction;
import org.citrusframework.agent.connector.actions.AgentConnectAction;
import org.citrusframework.agent.connector.actions.AgentRunAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class Agent implements TestActionBuilder<AbstractAgentAction> {

    private AbstractAgentAction.Builder<?, ?> delegate;

    private String agentName = CitrusAgentSettings.getAgentName();

    @SchemaProperty
    public void setName(String name) {
        this.agentName = name;
    }

    @SchemaProperty(kind = ACTION, group = "agent")
    public void setConnect(Connect connect) {
        AgentConnectAction.Builder builder = new AgentConnectAction.Builder();

        builder.url(connect.getUrl());
        builder.port(connect.getPort());

        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "agent")
    public void setRun(Run run) {
        AgentRunAction.Builder builder = new AgentRunAction.Builder();
        if (run.getSource() != null) {
            if (run.getSource().getCode() != null) {
                builder.sourceCode(run.getSource().getCode());
            }
        }

        delegate = builder;
    }

    @Override
    public AbstractAgentAction build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Citrus agent test action - please provide proper action details");
        }

        delegate.agent(agentName);

        return delegate.build();
    }

    public static class Connect {

        protected String url;
        protected int port = CitrusAgentSettings.getAgentServerPort();

        public String getUrl() {
            return url;
        }

        @SchemaProperty
        public void setUrl(String url) {
            this.url = url;
        }

        public int getPort() {
            return port;
        }

        @SchemaProperty
        public void setPort(int port) {
            this.port = port;
        }

    }

    public static class Run {

        private Source source;

        public Source getSource() {
            return source;
        }

        @SchemaProperty
        public void setSource(Source source) {
            this.source = source;
        }

        @SchemaProperty
        public void setActions(List<Map<String, Object>> actions) {
            this.source = new Source();
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("name", "${citrus.test.name}");
            raw.put("actions", actions);
            source.setCode(YamlSupport.dumpYaml(raw));
        }

        public static class Source {
            private String file;

            private String code;

            public String getFile() {
                return file;
            }

            @SchemaProperty
            public void setFile(String file) {
                this.file = file;
            }

            public String getCode() {
                return code;
            }

            @SchemaProperty
            public void setCode(String code) {
                this.code = code;
            }
        }
    }
}
