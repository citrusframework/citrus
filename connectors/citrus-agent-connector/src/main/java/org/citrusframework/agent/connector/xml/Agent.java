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

package org.citrusframework.agent.connector.xml;

import java.util.List;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.citrusframework.agent.connector.actions.AbstractAgentAction;
import org.citrusframework.agent.connector.actions.AgentConnectAction;
import org.citrusframework.agent.connector.actions.AgentRunAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.citrusframework.xml.StringResult;

@XmlRootElement(name = "agent")
public class Agent implements TestActionBuilder<AbstractAgentAction> {

    private AbstractAgentAction.Builder<?, ?> delegate;

    private String agentName = CitrusAgentSettings.getAgentName();

    @XmlAttribute
    public void setName(String name) {
        this.agentName = name;
    }

    @XmlElement
    public Agent setConnect(Connect connect) {
        AgentConnectAction.Builder builder = new AgentConnectAction.Builder();

        builder.url(connect.getUrl());
        builder.port(connect.getPort());

        delegate = builder;
        return this;
    }

    @XmlElement
    public Agent setRun(Run run) {
        AgentRunAction.Builder builder = new AgentRunAction.Builder();
        if (run.getSource() != null) {
            if (run.getSource().getCode() != null) {
                builder.sourceCode(run.getSource().getCode().trim());
            }
        }

        if (run.getActions() != null) {
            try {
                StringResult raw = new StringResult();
                Jaxb2Marshaller marshaller = new Jaxb2Marshaller(InlineTest.class);
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, true);
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                marshaller.marshal(new InlineTest(run.getActions()), raw);
                builder.sourceCode(raw.toString());
            } catch (JAXBException e) {
                throw new CitrusRuntimeException("Failed to process Citrus agent test actions", e);
            }
        }

        delegate = builder;
        return this;
    }

    @Override
    public AbstractAgentAction build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Citrus agent test action - please provide proper action details");
        }

        delegate.agent(agentName);

        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
    })
    public static class Connect {

        @XmlAttribute
        protected String url;

        @XmlAttribute
        protected int port = CitrusAgentSettings.getAgentServerPort();

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "source",
            "actions"
    })
    public static class Run {

        @XmlElement(name = "source")
        private Source source;

        @XmlElement
        protected TestActions actions;

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public TestActions getActions() {
            return actions;
        }

        public void setActions(TestActions actions) {
            this.actions = actions;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "actions"
        })
        public static class TestActions {
            @XmlAnyElement(lax = true)
            private List<Object> actions;

            public List<Object> getActions() {
                return actions;
            }

            public void setActions(List<Object> actions) {
                this.actions = actions;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "code"
        })
        public static class Source {
            @XmlAttribute
            private String file;

            @XmlElement
            private String code;

            public String getFile() {
                return file;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }
        }
    }

    @XmlRootElement(name = "test")
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "actions"
    })
    private static class InlineTest {

        @XmlAttribute
        private String name;

        @XmlElement
        private Run.TestActions actions;

        public InlineTest() {
            super();
        }

        private InlineTest(Run.TestActions actions) {
            this.name = "${citrus.test.name}";
            this.actions = actions;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Run.TestActions getActions() {
            return actions;
        }

        public void setActions(Run.TestActions actions) {
            this.actions = actions;
        }
    }
}
