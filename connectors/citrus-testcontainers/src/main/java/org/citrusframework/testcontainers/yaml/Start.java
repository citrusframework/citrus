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

package org.citrusframework.testcontainers.yaml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.testcontainers.aws2.LocalStackSettings;
import org.citrusframework.testcontainers.aws2.StartLocalStackAction;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.mongodb.StartMongoDBAction;
import org.citrusframework.testcontainers.postgresql.StartPostgreSQLAction;
import org.citrusframework.testcontainers.redpanda.StartRedpandaAction;
import org.citrusframework.util.ObjectHelper;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class Start extends AbstractTestcontainersAction.Builder<StartTestcontainersAction<?>, Start> implements ReferenceResolverAware {

    private StartTestcontainersAction.AbstractBuilder<?, ?, ?> delegate;

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setContainer(Container container) {
        StartTestcontainersAction.Builder<?> builder = new StartTestcontainersAction.Builder<>();
        configureStartActionBuilder(builder, container);
        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setLocalstack(LocalStack container) {
        StartLocalStackAction.Builder builder = new StartLocalStackAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        builder.autoCreateClients(container.isAutoCreateClients());

        if (container.getOptions() != null) {
            builder.withOptions(container.getOptions());
        }

        configureStartActionBuilder(builder, container);

        if (container.getServices() != null) {
            container.getServices().forEach(service -> builder.withService(AwsService.valueOf(service)));
        }

        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setMongodb(MongoDB container) {
        StartMongoDBAction.Builder builder = new StartMongoDBAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setKafka(Kafka container) {
        StartKafkaAction.Builder builder = new StartKafkaAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        if (container.getPort() > 0) {
            builder.port(container.getPort());
        }

        if (container.getImplementation() != null) {
            builder.implementation(container.getImplementation());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setRedpanda(Redpanda container) {
        StartRedpandaAction.Builder builder = new StartRedpandaAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = "testcontainers-start")
    public void setPostgresql(PostgreSQL container) {
        StartPostgreSQLAction.Builder builder = new StartPostgreSQLAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        if (container.getDataSourceName() != null) {
            builder.dataSourceName(container.getDataSourceName());
        }

        if (container.getDatabase() != null) {
            builder.databaseName(container.getDatabase());
        }

        if (container.getUsername() != null) {
            builder.username(container.getUsername());
        }

        if (container.getPassword() != null) {
            builder.password(container.getPassword());
        }

        if (container.getInitScript() != null) {
            if (container.getInitScript().getFile() != null) {
                builder.initScript(Resources.create(container.getInitScript().getFile()));
            }

            if (container.getInitScript().getValue() != null) {
                builder.initScript(container.getInitScript().getValue());
            }
        }

        delegate = builder;
    }

    @Override
    public Start description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Start actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public StartTestcontainersAction<?> doBuild() {
        ObjectHelper.assertNotNull(delegate);
        return delegate.build();
    }

    private void configureStartActionBuilder(StartTestcontainersAction.AbstractBuilder<?, ?, ?> builder, Container container) {
        builder.containerName(container.getName());
        builder.serviceName(container.getServiceName());
        builder.image(container.getImage());

        builder.autoRemove(container.isAutoRemove());

        if (container.getStartUpTimeout() > 0) {
            builder.withStartupTimeout(container.getStartUpTimeout());
        }

        if (container.getCommand() != null) {
            builder.withCommand(container.getCommand().split(" "));
        }

        container.getEnv().forEach(variable -> builder.withEnv(variable.getName(), variable.getValue()));

        container.getLabels().forEach(label -> builder.withLabel(label.getName(), label.getValue()));

        if (container.getWaitFor() != null) {
            if (container.getWaitFor().isDisabled()) {
                builder.waitStrategyDisabled();
            } else if (StringUtils.hasText(container.getWaitFor().getLogMessage())) {
                builder.waitFor(container.getWaitFor().getLogMessage());
            } else if (StringUtils.hasText(container.getWaitFor().getUrl())) {
                try {
                    builder.waitFor(new URL(container.getWaitFor().getUrl()));
                } catch (MalformedURLException e) {
                    throw new CitrusRuntimeException("Invalid Http(s) URL to wait for: %s".formatted(container.getWaitFor().getUrl()), e);
                }
            }
        }

        container.getExposedPorts().forEach(builder::addExposedPort);

        container.getPortBindings().forEach(builder::addPortBinding);

        container.getVolumeMounts().forEach(mount ->
                builder.withVolumeMount(mount.getFile(), mount.getMountPath()));
    }

    public static class Container {

        private String name;

        private String serviceName;

        private String image;

        private int startUpTimeout;

        protected String command;

        protected boolean autoRemove = TestContainersSettings.isAutoRemoveResources();

        protected List<Variable> env;

        protected List<Label> labels;

        protected WaitFor waitFor;

        protected List<Integer> exposedPorts;

        protected List<String> portBindings;

        protected List<VolumeMount> volumeMounts;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String name) {
            this.name = name;
        }

        public String getServiceName() {
            return serviceName;
        }

        @SchemaProperty
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getImage() {
            return image;
        }

        @SchemaProperty
        public void setImage(String image) {
            this.image = image;
        }

        public String getCommand() {
            return command;
        }

        @SchemaProperty
        public void setCommand(String command) {
            this.command = command;
        }

        public boolean isAutoRemove() {
            return autoRemove;
        }

        @SchemaProperty
        public void setAutoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
        }

        public int getStartUpTimeout() {
            return startUpTimeout;
        }

        @SchemaProperty
        public void setStartUpTimeout(int startUpTimeout) {
            this.startUpTimeout = startUpTimeout;
        }

        @SchemaProperty
        public void setEnv(List<Variable> variables) {
            this.env = variables;
        }

        public List<Variable> getEnv() {
            if (env == null) {
                env = new ArrayList<>();
            }
            return env;
        }

        public List<Label> getLabels() {
            if (labels == null) {
                labels = new ArrayList<>();
            }
            return labels;
        }

        @SchemaProperty
        public void setLabels(List<Label> labels) {
            this.labels = labels;
        }

        @SchemaProperty
        public void setWaitFor(WaitFor waitFor) {
            this.waitFor = waitFor;
        }

        public WaitFor getWaitFor() {
            return waitFor;
        }

        public List<Integer> getExposedPorts() {
            if (exposedPorts == null) {
                exposedPorts = new ArrayList<>();
            }
            return exposedPorts;
        }

        @SchemaProperty
        public void setExposedPorts(List<Integer> exposedPorts) {
            this.exposedPorts = exposedPorts;
        }

        public List<String> getPortBindings() {
            if (portBindings == null) {
                portBindings = new ArrayList<>();
            }
            return portBindings;
        }

        @SchemaProperty
        public void setPortBindings(List<String> portBindings) {
            this.portBindings = portBindings;
        }

        public List<VolumeMount> getVolumeMounts() {
            if (volumeMounts == null) {
                volumeMounts = new ArrayList<>();
            }
            return volumeMounts;
        }

        @SchemaProperty
        public void setVolumeMounts(List<VolumeMount> volumeMounts) {
            this.volumeMounts = volumeMounts;
        }
    }

    public static class LocalStack extends Container {

        protected boolean autoCreateClients = LocalStackSettings.isAutoCreateClients();

        protected String version;

        protected List<String> services;

        protected Map<String, String> options;

        public boolean isAutoCreateClients() {
            return autoCreateClients;
        }

        @SchemaProperty
        public void setAutoCreateClients(boolean autoCreateClients) {
            this.autoCreateClients = autoCreateClients;
        }

        public Map<String, String> getOptions() {
            return options;
        }

        @SchemaProperty
        public void setOptions(Map<String, String> options) {
            this.options = options;
        }

        public String getVersion() {
            return version;
        }

        @SchemaProperty
        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getServices() {
            if (services == null) {
                services = new ArrayList<>();
            }
            return services;
        }

        @SchemaProperty
        public void setServices(List<String> services) {
            this.services = services;
        }

    }

    public static class MongoDB extends Container {

        protected String version;

        public String getVersion() {
            return version;
        }

        @SchemaProperty
        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class Kafka extends Container {

        protected String version;
        protected String implementation;
        protected int port;

        public String getVersion() {
            return version;
        }

        @SchemaProperty
        public void setVersion(String version) {
            this.version = version;
        }

        public String getImplementation() {
            return implementation;
        }

        @SchemaProperty
        public void setImplementation(String implementation) {
            this.implementation = implementation;
        }

        public int getPort() {
            return port;
        }

        @SchemaProperty
        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Redpanda extends Container {

        protected String version;

        public String getVersion() {
            return version;
        }

        @SchemaProperty
        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class PostgreSQL extends Container {

        protected String version;

        protected String dataSourceName;

        protected String database;

        protected String username;

        protected String password;

        protected InitScript initScript;

        public String getVersion() {
            return version;
        }

        @SchemaProperty
        public void setVersion(String version) {
            this.version = version;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        @SchemaProperty
        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        public String getDatabase() {
            return database;
        }

        @SchemaProperty
        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUsername() {
            return username;
        }

        @SchemaProperty
        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        @SchemaProperty
        public void setPassword(String password) {
            this.password = password;
        }

        public InitScript getInitScript() {
            return initScript;
        }

        @SchemaProperty
        public void setInitScript(InitScript initScript) {
            this.initScript = initScript;
        }

        public static class InitScript {

            protected String file;
            protected String value;

            public String getFile() {
                return file;
            }

            @SchemaProperty
            public void setFile(String file) {
                this.file = file;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty
            public void setValue(String value) {
                this.value = value;
            }

        }
    }

    public static class WaitFor {

        private String logMessage;

        private String url;

        private boolean disabled;

        public String getLogMessage() {
            return logMessage;
        }

        @SchemaProperty
        public void setLogMessage(String logMessage) {
            this.logMessage = logMessage;
        }

        public String getUrl() {
            return url;
        }

        @SchemaProperty
        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isDisabled() {
            return disabled;
        }

        @SchemaProperty
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }

    public static class VolumeMount {

        protected String file;
        protected String mountPath;

        public String getFile() {
            return file;
        }

        @SchemaProperty
        public void setFile(String file) {
            this.file = file;
        }

        public String getMountPath() {
            return mountPath;
        }

        @SchemaProperty
        public void setMountPath(String mountPath) {
            this.mountPath = mountPath;
        }
    }

    public static class Variable {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(String value) {
            this.value = value;
        }

    }

    public static class Label {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(String value) {
            this.value = value;
        }

    }
}
