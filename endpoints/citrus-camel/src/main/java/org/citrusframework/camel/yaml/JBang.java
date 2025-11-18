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

package org.citrusframework.camel.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.AbstractCamelJBangAction;
import org.citrusframework.camel.actions.AddCamelPluginAction;
import org.citrusframework.camel.actions.CamelCmdReceiveAction;
import org.citrusframework.camel.actions.CamelCmdSendAction;
import org.citrusframework.camel.actions.CamelCustomizedRunIntegrationAction;
import org.citrusframework.camel.actions.CamelKubernetesDeleteIntegrationAction;
import org.citrusframework.camel.actions.CamelKubernetesRunIntegrationAction;
import org.citrusframework.camel.actions.CamelKubernetesVerifyIntegrationAction;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;
import static org.citrusframework.yaml.SchemaProperty.Kind.GROUP;

public class JBang implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

    private static final String CAMEL_JBANG_GROUP = "camel-jbang";

    private AbstractCamelJBangAction.Builder<?, ?> builder;

    private String camelVersion;
    private String kameletsVersion;

    @SchemaProperty(description = "The Apache Camel version.")
    public void setCamelVersion(String camelVersion) {
        this.camelVersion = camelVersion;
    }

    public String getCamelVersion() {
        return camelVersion;
    }

    @SchemaProperty(description = "The Kamelets version.")
    public void setKameletsVersion(String kameletsVersion) {
        this.kameletsVersion = kameletsVersion;
    }

    public String getKameletsVersion() {
        return kameletsVersion;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_GROUP, description = "Runs a Camel integration.")
    public void setRun(RunIntegration run) {
        this.builder = run.getBuilder();
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_GROUP, description = "Stops a Camel integration.")
    public void setStop(StopIntegration stop) {
        this.builder = stop.getBuilder();
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_GROUP, description = "Runs a Camel integration with customized parameters.")
    public void setCustom(CustomizedRunIntegration custom) {
        this.builder = custom.getBuilder();
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_GROUP, description = "Verify the Camel integration status and log messages.")
    public void setVerify(VerifyIntegration verify) {
        this.builder = verify.getBuilder();
    }

    @SchemaProperty(kind = GROUP, group = CAMEL_JBANG_GROUP, description = "Manage Camel JBang plugins.")
    public void setPlugin(Plugin plugin) {
        this.builder = plugin.getBuilder();
    }

    @SchemaProperty(kind = GROUP, group = CAMEL_JBANG_GROUP, description = "Camel JBang cmd operations.")
    public void setCmd(Cmd cmd) {
        this.builder = cmd.getBuilder();
    }

    @SchemaProperty(kind = GROUP, group = CAMEL_JBANG_GROUP, description = "Camel Kubernetes plugin related operations.")
    public void setKubernetes(Kubernetes kubernetes) {
        this.builder = kubernetes.getBuilder();
    }

    @Override
    public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Camel JBang action specification");
        }

        builder.camelVersion(camelVersion);
        builder.kameletsVersion(kameletsVersion);

        return builder;
    }

    public static class RunIntegration implements CamelActionBuilderWrapper<CamelRunIntegrationAction.Builder> {

        private final CamelRunIntegrationAction.Builder builder = new CamelRunIntegrationAction.Builder();

        @SchemaProperty(advanced = true, description = "When enabled the Camel integration is automatically stopped after the test.", defaultValue = "true")
        public void setAutoRemove(boolean enabled) {
            builder.autoRemove(enabled);
        }

        @SchemaProperty(advanced = true, description = "Wait for the integration to report running state.", defaultValue = "true")
        public void setWaitForRunningState(boolean enabled) {
            builder.waitForRunningState(enabled);
        }

        @SchemaProperty(advanced = true, description = "Optional command arguments.")
        public void setArgs(List<String> args) {
            builder.withArgs(args.toArray(String[]::new));
        }

        @SchemaProperty(advanced = true, description = "Optional stubbed components.")
        public void setStub(List<String> stub) {
            builder.stub(stub.toArray(String[]::new));
        }

        @SchemaProperty(advanced = true, description = "Optional list of resources added to the Camel JBang process.")
        public void setResources(List<String> resources) {
            resources.forEach(builder::addResource);
        }

        @SchemaProperty(advanced = true, description = "When enabled the integration output is saved to a log file.", defaultValue = "false")
        public void setDumpIntegrationOutput(boolean enabled) {
            builder.dumpIntegrationOutput(enabled);
        }

        @SchemaProperty(description = "The Camel integration.")
        public void setIntegration(Integration integration) {
            builder.integrationName(integration.name);

            if (integration.file != null) {
                builder.integration(Resources.create(integration.file));
            }

            if (integration.sourceCode != null) {
                builder.integration(integration.sourceCode);
            }

            if (integration.systemProperties != null) {
                if (integration.systemProperties.getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            integration.systemProperties.getFile()));
                }

                integration.systemProperties
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (integration.environment != null) {
                if (integration.environment.getFile() != null) {
                    builder.withEnvs(Resources.create(
                            integration.environment.getFile()));
                }

                integration.environment
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }
        }

        public static class Integration {
            private String name;
            private String file;
            private String sourceCode;

            protected Environment environment;

            protected SystemProperties systemProperties;

            @SchemaProperty(description = "The Camel integration name.")
            public void setName(String integrationName) {
                this.name = integrationName;
            }

            @SchemaProperty(description = "Camel integration source code loaded from a file resource.")
            public void setFile(String file) {
                this.file = file;
            }

            @SchemaProperty(description = "Camel integration source code.")
            public void setSources(String sourceCode) {
                this.sourceCode = sourceCode;
            }

            public Environment getEnvironment() {
                return this.environment;
            }

            @SchemaProperty(advanced = true, description = "Environment variables set for the Camel JBang process.")
            public void setEnvironment(Environment environment) {
                this.environment = environment;
            }

            public SystemProperties getSystemProperties() {
                return this.systemProperties;
            }

            @SchemaProperty(advanced = true, description = "System properties set for the Camel JBang process.")
            public void setSystemProperties(SystemProperties systemProperties) {
                this.systemProperties = systemProperties;
            }

            public static class Environment {

                protected String file;

                protected List<Variable> variables;

                public List<Variable> getVariables() {
                    if (variables == null) {
                        variables = new ArrayList<>();
                    }
                    return this.variables;
                }

                @SchemaProperty(description = "List of environment variables.")
                public void setVariables(List<Variable> variables) {
                    this.variables = variables;
                }

                @SchemaProperty(description = "Environment variables loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public static class Variable {

                    protected String name;
                    protected String value;

                    public String getName() {
                        return name;
                    }

                    @SchemaProperty(required = true, description = "The environment variable name.")
                    public void setName(String value) {
                        this.name = value;
                    }

                    public String getValue() {
                        return value;
                    }

                    @SchemaProperty(required = true, description = "The environment variable value.")
                    public void setValue(String value) {
                        this.value = value;
                    }

                }
            }

            public static class SystemProperties {

                protected String file;

                protected List<Property> properties;

                public List<Property> getProperties() {
                    if (properties == null) {
                        properties = new ArrayList<>();
                    }
                    return this.properties;
                }

                @SchemaProperty(description = "List of system properties.")
                public void setProperties(List<Property> properties) {
                    this.properties = properties;
                }

                @SchemaProperty(description = "System properties loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public static class Property {

                    protected String name;
                    protected String value;

                    public String getName() {
                        return name;
                    }

                    @SchemaProperty(required = true, description = "The system property name.")
                    public void setName(String value) {
                        this.name = value;
                    }

                    public String getValue() {
                        return value;
                    }

                    @SchemaProperty(required = true, description = "The system property value.")
                    public void setValue(String value) {
                        this.value = value;
                    }

                }
            }
        }

        @Override
        public CamelRunIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class StopIntegration implements CamelActionBuilderWrapper<CamelStopIntegrationAction.Builder> {

        private final CamelStopIntegrationAction.Builder builder = new CamelStopIntegrationAction.Builder();

        @SchemaProperty(description = "Camel integration name")
        public void setIntegration(String integrationName) {
            builder.integrationName(integrationName);
        }

        @Override
        public CamelStopIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class CustomizedRunIntegration implements CamelActionBuilderWrapper<CamelCustomizedRunIntegrationAction.Builder> {

        private final CamelCustomizedRunIntegrationAction.Builder builder = new CamelCustomizedRunIntegrationAction.Builder();

        @SchemaProperty(description = "Customized Camel JBang commands.")
        public void setCommands(List<String> commands) {
            builder.commands(commands.toArray(new String[0]));
        }

        @SchemaProperty(advanced = true, description = "Custom working directory for the Camel JBang process.")
        public void setWorkDir(String workDir) {
            builder.workDir(workDir);
        }

        @SchemaProperty(advanced = true, description = "Custom Camel JBang process name.")
        public void setProcessName(String processName) {
            builder.processName(processName);
        }

        @SchemaProperty(advanced = true, description = "When enabled the Camel integration is automatically stopped after the test.", defaultValue = "true")
        public void setAutoRemove(boolean enabled) {
            builder.autoRemove(enabled);
        }

        @SchemaProperty(advanced = true, description = "Wait for the integration to report running state.", defaultValue = "true")
        public void setWaitForRunningState(boolean enabled) {
            builder.waitForRunningState(enabled);
        }

        @SchemaProperty(advanced = true, description = "Optional command arguments.")
        public void setArgs(List<String> args) {
            builder.withArgs(args.toArray(String[]::new));
        }

        @SchemaProperty(advanced = true, description = "Optional list of resources added to the Camel JBang process.")
        public void setResources(List<String> resources) {
            resources.forEach(builder::addResource);
        }

        @SchemaProperty(advanced = true, description = "When enabled the integration output is saved to a log file.", defaultValue = "false")
        public void setDumpIntegrationOutput(boolean enabled) {
            builder.dumpIntegrationOutput(enabled);
        }

        @SchemaProperty(description = "The Camel integration.")
        public void setIntegration(Integration integration) {
            builder.processName(integration.name);

            if (integration.file != null) {
                builder.addResource(integration.file);
            }

            if (integration.systemProperties != null) {
                if (integration.systemProperties.getFile() != null) {
                    builder.withSystemProperties(Resources.create(
                            integration.systemProperties.getFile()));
                }

                integration.systemProperties
                        .getProperties()
                        .forEach(property -> builder.withSystemProperty(property.getName(), property.getValue()));
            }

            if (integration.environment != null) {
                if (integration.environment.getFile() != null) {
                    builder.withEnvs(Resources.create(
                            integration.environment.getFile()));
                }

                integration.environment
                        .getVariables()
                        .forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));
            }
        }

        public static class Integration {
            private String name;
            private String file;

            protected Environment environment;

            protected SystemProperties systemProperties;

            @SchemaProperty(description = "The Camel integration name.")
            public void setName(String integrationName) {
                this.name = integrationName;
            }

            @SchemaProperty(description = "Camel integration source code loaded from a file resource.")
            public void setFile(String file) {
                this.file = file;
            }

            public Environment getEnvironment() {
                return this.environment;
            }

            @SchemaProperty(advanced = true, description = "Environment variables set for the Camel JBang process.")
            public void setEnvironment(Environment environment) {
                this.environment = environment;
            }

            public SystemProperties getSystemProperties() {
                return this.systemProperties;
            }

            @SchemaProperty(advanced = true, description = "System properties set for the Camel JBang process.")
            public void setSystemProperties(SystemProperties systemProperties) {
                this.systemProperties = systemProperties;
            }

            public static class Environment {

                protected String file;

                protected List<Environment.Variable> variables;

                public List<Environment.Variable> getVariables() {
                    if (variables == null) {
                        variables = new ArrayList<>();
                    }
                    return this.variables;
                }

                @SchemaProperty(description = "List of environment variables.")
                public void setVariables(List<Variable> variables) {
                    this.variables = variables;
                }

                @SchemaProperty(description = "Environment variables loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public static class Variable {

                    protected String name;
                    protected String value;

                    public String getName() {
                        return name;
                    }

                    @SchemaProperty(required = true, description = "The environment variable name.")
                    public void setName(String value) {
                        this.name = value;
                    }

                    public String getValue() {
                        return value;
                    }

                    @SchemaProperty(required = true, description = "The environment variable value.")
                    public void setValue(String value) {
                        this.value = value;
                    }

                }
            }

            public static class SystemProperties {

                protected String file;

                protected List<SystemProperties.Property> properties;

                public List<SystemProperties.Property> getProperties() {
                    if (properties == null) {
                        properties = new ArrayList<>();
                    }
                    return this.properties;
                }

                @SchemaProperty(description = "List of system properties.")
                public void setProperties(List<Property> properties) {
                    this.properties = properties;
                }

                @SchemaProperty(description = "System properties loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public static class Property {

                    protected String name;
                    protected String value;

                    public String getName() {
                        return name;
                    }

                    @SchemaProperty(required = true, description = "The system property name.")
                    public void setName(String value) {
                        this.name = value;
                    }

                    public String getValue() {
                        return value;
                    }

                    @SchemaProperty(required = true, description = "The system property value.")
                    public void setValue(String value) {
                        this.value = value;
                    }

                }
            }
        }

        @Override
        public CamelCustomizedRunIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class VerifyIntegration implements CamelActionBuilderWrapper<CamelVerifyIntegrationAction.Builder> {

        private final CamelVerifyIntegrationAction.Builder builder = new CamelVerifyIntegrationAction.Builder();

        @SchemaProperty(description = "The Camel integration name.")
        public void setIntegration(String integrationName) {
            builder.integrationName(integrationName);
        }

        @SchemaProperty(description = "The expected log message to verify.")
        public void setLogMessage(String logMessage) {
            builder.waitForLogMessage(logMessage);
        }

        @SchemaProperty(advanced = true, description = "The expected integration status", defaultValue = "Running")
        public void setPhase(String phase) {
            builder.isInPhase(phase);
        }

        @SchemaProperty(advanced = true, description = "When enabled the Camel integration log output is added to the test log output.")
        public void setPrintLogs(boolean printLogs) {
            builder.printLogs(printLogs);
        }

        @SchemaProperty(advanced = true, description = "The delay in milliseconds to wait between validation attempts.", defaultValue = "1000")
        public void setDelayBetweenAttempts(long delayBetweenAttempts) {
            builder.delayBetweenAttempts(delayBetweenAttempts);
        }

        @SchemaProperty(advanced = true, description = "Maximum attempts to validate the integration.", defaultValue = "60")
        public void setMaxAttempts(int maxAttempts) {
            builder.maxAttempts(maxAttempts);
        }

        @SchemaProperty(advanced = true, description = "When enabled the validation attempts stop when error state is reported.", defaultValue = "true")
        public void setStopOnErrorStatus(boolean stopOnErrorStatus) {
            builder.stopOnErrorStatus(stopOnErrorStatus);
        }

        @Override
        public CamelVerifyIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class Plugin implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

        private static final String CAMEL_JBANG_PLUGIN_GROUP = "camel-jbang-plugin";

        private AbstractCamelJBangAction.Builder<?, ?> builder;

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_PLUGIN_GROUP, description = "Adds a plugin to the Camel Jbang installation.")
        public void setAdd(Add add) {
            AddCamelPluginAction.Builder builder = new AddCamelPluginAction.Builder();
            builder.pluginName(add.getName());
            if (add.getArgs() != null) {
                add.getArgs().forEach(builder::withArg);
            }
            this.builder = builder;
        }

        @Override
        public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
            return builder;
        }

        public static class Add {
            protected String name;

            protected List<String> args;

            public String getName() {
                return name;
            }

            @SchemaProperty(required = true, description = "The plugin name.")
            public void setName(String name) {
                this.name = name;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            @SchemaProperty(advanced = true, description = "Plugin arguments.")
            public void setArgs(List<String> args) {
                this.args = args;
            }
        }
    }

    public static class Cmd implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

        private static final String CAMEL_JBANG_CMD_GROUP = "camel-jbang-cmd";

        private AbstractCamelJBangAction.Builder<?, ?> builder;

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_CMD_GROUP, description = "Sends a message to Camel JBang integration.")
        public void setSend(Send send) {
            CamelCmdSendAction.Builder builder = new CamelCmdSendAction.Builder();

            builder.timeout(send.getTimeout());

            if (send.getHeaders() != null) {
                for (Map.Entry<String, String> header : send.getHeaders().entrySet()) {
                    builder.header(header.getKey(), header.getValue());
                }
            }

            builder.integration(send.getIntegration());

            if (send.getBody() != null) {
                if (send.getBody().getData() != null) {
                    builder.body(send.getBody().getData());
                } else if (send.getBody().getFile() != null) {
                    builder.body("file:" + send.getBody().getFile());
                }
            }

            if (send.getEndpoint() != null) {
                builder.endpoint(send.getEndpoint());
            }

            if (send.getUri() != null) {
                builder.endpointUri(send.getUri());
            }

            if (send.getArgs() != null) {
                send.getArgs().forEach(builder::withArg);
            }

            builder.reply(send.isReply());

            this.builder = builder;
        }

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_CMD_GROUP, description = "Receives a message from a Camel JBang integration.")
        public void setReceive(Receive receive) {
            CamelCmdReceiveAction.Builder builder = new CamelCmdReceiveAction.Builder();

            builder.integration(receive.getIntegration());

            if (receive.getEndpoint() != null) {
                builder.endpoint(receive.getEndpoint());
            }

            if (receive.getUri() != null) {
                builder.endpointUri(receive.getUri());
            }

            if (receive.getArgs() != null) {
                receive.getArgs().forEach(builder::withArg);
            }

            if (receive.getGrep() != null) {
                builder.grep(receive.getGrep());
            }

            builder.loggingColor(receive.isLoggingColor());

            if (receive.getSince() != null) {
                builder.since(receive.getSince());
            }

            if (receive.getTail() != null) {
                builder.tail(receive.getTail());
            }

            builder.maxAttempts(receive.getMaxAttempts());
            builder.delayBetweenAttempts(receive.getDelayBetweenAttempts());

            builder.printLogs(receive.isPrintLogs());
            builder.stopOnErrorStatus(receive.isStopOnErrorStatus());

            this.builder = builder;
        }

        @Override
        public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
            return builder;
        }

        public static class Send {

            protected String timeout = "20000";
            protected String integration;
            protected String endpoint;
            protected String uri;
            protected List<String> args;
            protected boolean reply;

            protected Map<String, String> headers;
            protected Body body;

            @SchemaProperty(description = "The send timeout.")
            public void setTimeout(String timeout) {
                this.timeout = timeout;
            }

            public String getTimeout() {
                return timeout;
            }

            @SchemaProperty(description = "The message headers.")
            public void setHeaders(Map<String, String> headers) {}

            public Map<String, String> getHeaders() {
                if (headers == null) {
                    headers = new HashMap<>();
                }

                return headers;
            }

            public String getIntegration() {
                return integration;
            }

            @SchemaProperty(description = "The Camel integration to send the message to.")
            public void setIntegration(String integration) {
                this.integration = integration;
            }

            public String getEndpoint() {
                return endpoint;
            }

            @SchemaProperty(advanced = true, description = "Optional endpoint in the Camel integration.")
            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getUri() {
                return uri;
            }

            @SchemaProperty(advanced = true, description = "Camel endpoint URI to send message to.")
            public void setUri(String uri) {
                this.uri = uri;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            @SchemaProperty(advanced = true, description = "Command arguments.")
            public void setArgs(List<String> args) {
                this.args = args;
            }

            @SchemaProperty(description = "The message body.")
            public void setBody(Body body) {
                this.body = body;
            }

            public Body getBody() {
                return body;
            }

            public boolean isReply() {
                return reply;
            }

            @SchemaProperty(advanced = true, description = "When enabled the operation waits for a reply message from the integration.")
            public void setReply(boolean reply) {
                this.reply = reply;
            }

            public static class Body {

                protected String file;

                protected String data;

                public String getData() {
                    return data;
                }

                @SchemaProperty(description = "The message body as inline data.")
                public void setData(String data) {
                    this.data = data;
                }

                public String getFile() {
                    return file;
                }

                @SchemaProperty(description = "The message body content loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }
            }
        }

        public static class Receive {

            protected String integration;
            protected String endpoint;
            protected String uri;
            protected List<String> args;
            protected boolean loggingColor;

            protected String grep;
            protected String since;
            protected String tail;

            protected int maxAttempts = CamelSettings.getMaxAttempts();
            protected long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

            protected boolean printLogs = CamelSettings.isPrintLogs();
            protected boolean stopOnErrorStatus = true;

            public String getIntegration() {
                return integration;
            }

            @SchemaProperty(description = "The Camel integration to send the message to.")
            public void setIntegration(String integration) {
                this.integration = integration;
            }

            public String getEndpoint() {
                return endpoint;
            }

            @SchemaProperty(advanced = true, description = "Optional endpoint in the Camel integration.")
            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getUri() {
                return uri;
            }

            @SchemaProperty(advanced = true, description = "Camel endpoint URI to send message to.")
            public void setUri(String uri) {
                this.uri = uri;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            @SchemaProperty(advanced = true, description = "Command arguments.")
            public void setArgs(List<String> args) {
                this.args = args;
            }

            public boolean isLoggingColor() {
                return loggingColor;
            }

            @SchemaProperty(advanced = true, description = "When enabled the output uses logging color.", defaultValue = "false")
            public void setLoggingColor(boolean loggingColor) {
                this.loggingColor = loggingColor;
            }

            public String getGrep() {
                return grep;
            }

            @SchemaProperty(description = "Filter messages based on this expression.")
            public void setGrep(String grep) {
                this.grep = grep;
            }

            public String getSince() {
                return since;
            }

            @SchemaProperty(advanced = true, description = "Return messages newer than a relative duration.")
            public void setSince(String since) {
                this.since = since;
            }

            public String getTail() {
                return tail;
            }

            @SchemaProperty(advanced = true, description = "The number of messages from the end to show.", defaultValue = "0")
            public void setTail(String tail) {
                this.tail = tail;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            @SchemaProperty(advanced = true, description = "Maximum number of validation attempts.", defaultValue = "60")
            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public long getDelayBetweenAttempts() {
                return delayBetweenAttempts;
            }

            @SchemaProperty(advanced = true, description = "The delay in milliseconds to wait between validation attempts.", defaultValue = "1000")
            public void setDelayBetweenAttempts(long delayBetweenAttempts) {
                this.delayBetweenAttempts = delayBetweenAttempts;
            }

            public boolean isPrintLogs() {
                return printLogs;
            }

            @SchemaProperty(advanced = true, description = "When enabled the Camel integration log output is added to the test log output.")
            public void setPrintLogs(boolean printLogs) {
                this.printLogs = printLogs;
            }

            @SchemaProperty(advanced = true, description = "When enabled the validation attempts stop when error state is reported.", defaultValue = "true")
            public void setStopOnErrorStatus(boolean stopOnErrorStatus) {
                this.stopOnErrorStatus = stopOnErrorStatus;
            }

            public boolean isStopOnErrorStatus() {
                return stopOnErrorStatus;
            }
        }
    }

    public static class Kubernetes implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

        private static final String CAMEL_JBANG_KUBERNETES_GROUP = "camel-jbang-kubernetes";

        private AbstractCamelJBangAction.Builder<?, ?> builder;

        @Override
        public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
            return builder;
        }

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_KUBERNETES_GROUP, description = "Runs a Camel integration on Kubernetes.")
        public void setRun(Run run) {
            CamelKubernetesRunIntegrationAction.Builder builder = new CamelKubernetesRunIntegrationAction.Builder();
            if (run.getIntegration().getFile() != null) {
                builder.integration(Resources.create(run.getIntegration().getFile()));
            }
            if (run.getRuntime() != null) {
                builder.runtime(run.getRuntime());
            }
            if (run.getImageBuilder() != null) {
                builder.imageBuilder(run.getImageBuilder());
            }
            if (run.getImageRegistry() != null) {
                builder.imageRegistry(run.getImageRegistry());
            }
            if (run.getClusterType() != null) {
                builder.clusterType(run.getClusterType());
            }
            if (run.getBuildProperties() != null) {
                run.getBuildProperties().forEach(builder::withBuildProperty);
            }
            if (run.getProperties() != null) {
                run.getProperties().forEach(builder::withProperty);
            }
            if (run.getTraits() != null) {
                run.getTraits().forEach(builder::withTrait);
            }
            if (run.getArgs() != null) {
                run.getArgs().forEach(builder::withArg);
            }
            if (run.getArgLine() != null) {
                builder.withArgs(run.getArgLine().split(" "));
            }

            builder.verbose(run.isVerbose());

            builder.autoRemove(run.isAutoRemove());
            builder.waitForRunningState(run.isWaitForRunningState());
            this.builder = builder;
        }

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_KUBERNETES_GROUP, description = "Verify the status and log messages for a Camel integration run on Kubernetes.")
        public void setVerify(Verify verify) {
            CamelKubernetesVerifyIntegrationAction.Builder builder = new CamelKubernetesVerifyIntegrationAction.Builder();
            builder.integration(verify.getIntegration())
                    .label(verify.getLabel())
                    .namespace(verify.getNamespace())
                    .waitForLogMessage(verify.getLogMessage())
                    .maxAttempts(verify.getMaxAttempts())
                    .delayBetweenAttempts(verify.getDelayBetweenAttempts())
                    .printLogs(verify.isPrintLogs());

            if (verify.getArgs() != null) {
                verify.getArgs().forEach(builder::withArg);
            }
            this.builder = builder;
        }

        @SchemaProperty(kind = ACTION, group = CAMEL_JBANG_KUBERNETES_GROUP, description = "Deletes a Camel integration from Kubernetes.")
        public void setDelete(Delete delete) {
            CamelKubernetesDeleteIntegrationAction.Builder builder = new CamelKubernetesDeleteIntegrationAction.Builder();
            builder.clusterType(delete.getClusterType())
                    .workingDir(delete.getWorkingDir())
                    .namespace(delete.getNamespace());
            if (delete.getIntegration().getFile() != null) {
                builder.integration(Resources.create(delete.getIntegration().getFile()));
            }
            if (delete.getIntegration().getName() != null) {
                builder.integration(delete.getIntegration().getName());
            }
            this.builder = builder;
        }

        public static class Run {

            protected Integration integration;
            protected String runtime;
            protected String imageRegistry;
            protected String imageBuilder;
            protected String clusterType;

            protected List<String> buildProperties;
            protected List<String> properties;
            protected List<String> traits;
            protected List<String> args;
            protected String argLine;

            protected boolean verbose;

            protected boolean autoRemove;
            protected boolean waitForRunningState = CamelJBangSettings.isWaitForRunningState();

            public Integration getIntegration() {
                return integration;
            }

            @SchemaProperty(description = "The Camel integration name.")
            public void setIntegration(Integration integration) {
                this.integration = integration;
            }

            public String getRuntime() {
                return runtime;
            }

            @SchemaProperty(advanced = true, description = "The runtime to use.", defaultValue = "quarkus")
            public void setRuntime(String runtime) {
                this.runtime = runtime;
            }

            public String getImageRegistry() {
                return imageRegistry;
            }

            @SchemaProperty(advanced = true, description = "The Docker image registry.")
            public void setImageRegistry(String imageRegistry) {
                this.imageRegistry = imageRegistry;
            }

            public String getImageBuilder() {
                return imageBuilder;
            }

            @SchemaProperty(advanced = true, description = "The Docker image builder.")
            public void setImageBuilder(String imageBuilder) {
                this.imageBuilder = imageBuilder;
            }

            public String getClusterType() {
                return clusterType;
            }

            @SchemaProperty(advanced = true, description = "The Kubernetes cluster type.")
            public void setClusterType(String clusterType) {
                this.clusterType = clusterType;
            }

            public List<String> getBuildProperties() {
                if (buildProperties == null) {
                    buildProperties = new ArrayList<>();
                }
                return this.buildProperties;
            }

            @SchemaProperty(advanced = true, description = "List of build properties passed to the Camel JBang project build.")
            public void setBuildProperties(List<String> buildProperties) {
                this.buildProperties = buildProperties;
            }

            public List<String> getProperties() {
                if (properties == null) {
                    properties = new ArrayList<>();
                }
                return this.properties;
            }

            @SchemaProperty(description = "Properties set on the Camel JBang project export.")
            public void setProperties(List<String> properties) {
                this.properties = properties;
            }

            @SchemaProperty(description = "List of traits to set for the project export.")
            public void setTraits(List<String> traits) {
                this.traits = traits;
            }

            public List<String> getTraits() {
                if (traits == null) {
                    traits = new ArrayList<>();
                }
                return this.traits;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            @SchemaProperty(advanced = true, description = "Camel JBang command arguments.")
            public void setArgs(List<String> args) {
                this.args = args;
            }

            public boolean isVerbose() {
                return verbose;
            }

            @SchemaProperty(advanced = true, description = "When enabled the Camel JBang command print verbose log messages.", defaultValue = "false")
            public void setVerbose(boolean verbose) {
                this.verbose = verbose;
            }

            public boolean isAutoRemove() {
                return autoRemove;
            }

            @SchemaProperty(description = "When enabled the Camel integration is automatically removed from Kubernetes after the test.", defaultValue = "true")
            public void setAutoRemove(boolean autoRemove) {
                this.autoRemove = autoRemove;
            }

            public String getArgLine() {
                return argLine;
            }

            @SchemaProperty(advanced = true, description = "Camel JBang command arguments.")
            public void setArgLine(String argLine) {
                this.argLine = argLine;
            }

            public boolean isWaitForRunningState() {
                return waitForRunningState;
            }

            @SchemaProperty(advanced = true, description = "When enabled the test waits for the Camel integration Kubernetes deployment to report proper running state.", defaultValue = "true")
            public void setWaitForRunningState(boolean enabled) {
                this.waitForRunningState = enabled;
            }

            public static class Integration {
                protected String file;

                @SchemaProperty(description = "The integration source code loaded as a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }
            }
        }

        public static class Verify {
            protected String integration;
            protected String label;
            private String namespace;

            private String logMessage;
            private int maxAttempts = CamelSettings.getMaxAttempts();
            private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

            private boolean printLogs = CamelSettings.isPrintLogs();

            protected List<String> args;

            public String getIntegration() {
                return integration;
            }

            @SchemaProperty(description = "The Camel integration name.")
            public void setIntegration(String integration) {
                this.integration = integration;
            }

            public String getLabel() {
                return label;
            }

            @SchemaProperty(description = "Identify the Camel integration via a Kubernetes label.")
            public void setLabel(String label) {
                this.label = label;
            }

            public String getNamespace() {
                return namespace;
            }

            @SchemaProperty(advanced = true, description = "The Kubernetes namespace.")
            public void setNamespace(String namespace) {
                this.namespace = namespace;
            }

            public String getLogMessage() {
                return logMessage;
            }

            @SchemaProperty(description = "The expected log message to verify.")
            public void setLogMessage(String logMessage) {
                this.logMessage = logMessage;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            @SchemaProperty(advanced = true, description = "Maximum number of validation attempts.", defaultValue = "60")
            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public long getDelayBetweenAttempts() {
                return delayBetweenAttempts;
            }

            @SchemaProperty(advanced = true, description = "The delay in milliseconds to wait between validation attempts.", defaultValue = "1000")
            public void setDelayBetweenAttempts(long delayBetweenAttempts) {
                this.delayBetweenAttempts = delayBetweenAttempts;
            }

            public boolean isPrintLogs() {
                return printLogs;
            }

            @SchemaProperty(advanced = true, description = "When enabled the Camel integration log output is added to the test log output.")
            public void setPrintLogs(boolean printLogs) {
                this.printLogs = printLogs;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            @SchemaProperty(advanced = true, description = "Camel JBang command arguments.")
            public void setArgs(List<String> args) {
                this.args = args;
            }
        }

        public static class Delete {
            protected Integration integration;

            protected String clusterType;
            protected String workingDir;
            protected String namespace;

            public String getClusterType() {
                return clusterType;
            }

            @SchemaProperty(advanced = true, description = "The Kubernetes cluster type.")
            public void setClusterType(String clusterType) {
                this.clusterType = clusterType;
            }

            public String getWorkingDir() {
                return workingDir;
            }

            @SchemaProperty(advanced = true, description = "The working directory that contains the Kubernetes manifest.")
            public void setWorkingDir(String workingDir) {
                this.workingDir = workingDir;
            }

            public String getNamespace() {
                return namespace;
            }

            @SchemaProperty(advanced = true, description = "The Kubernetes namespace.")
            public void setNamespace(String namespace) {
                this.namespace = namespace;
            }

            public Integration getIntegration() {
                return integration;
            }

            @SchemaProperty(description = "The Camel integration to delete.")
            public void setIntegration(Integration integration) {
                this.integration = integration;
            }


            public static class Integration {
                protected String file;
                protected String name;

                @SchemaProperty(description = "The Camel integration source code loaded from a file resource.")
                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public String getName() {
                    return name;
                }

                @SchemaProperty(description = "The Camel integration name.")
                public void setName(String name) {
                    this.name = name;
                }
            }
        }
    }

}
