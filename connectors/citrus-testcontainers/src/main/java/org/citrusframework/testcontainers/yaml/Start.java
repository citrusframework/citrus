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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.StartLocalStackAction;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.mongodb.StartMongoDBAction;
import org.citrusframework.testcontainers.postgresql.StartPostgreSQLAction;
import org.citrusframework.testcontainers.redpanda.StartRedpandaAction;
import org.citrusframework.util.ObjectHelper;

public class Start extends AbstractTestcontainersAction.Builder<StartTestcontainersAction<?>, Start> implements ReferenceResolverAware {

    private StartTestcontainersAction.AbstractBuilder<?, ?, ?> delegate;

    public void setContainer(Container container) {
        StartTestcontainersAction.Builder<?> builder = new StartTestcontainersAction.Builder<>();
        configureStartActionBuilder(builder, container);
        delegate = builder;
    }

    public void setLocalstack(LocalStack container) {
        StartLocalStackAction.Builder builder = new StartLocalStackAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        if (container.getServices() != null) {
            container.getServices().forEach(service -> builder.withService(LocalStackContainer.Service.valueOf(service)));
        }

        delegate = builder;
    }

    public void setMongodb(MongoDB container) {
        StartMongoDBAction.Builder builder = new StartMongoDBAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

    public void setKafka(Kafka container) {
        StartKafkaAction.Builder builder = new StartKafkaAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

    public void setRedpanda(Redpanda container) {
        StartRedpandaAction.Builder builder = new StartRedpandaAction.Builder();
        if (container.getVersion() != null) {
            builder.version(container.getVersion());
        }

        configureStartActionBuilder(builder, container);

        delegate = builder;
    }

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

        protected List<Integer> exposedPorts;

        protected List<String> portBindings;

        protected List<VolumeMount> volumeMounts;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public boolean isAutoRemove() {
            return autoRemove;
        }

        public void setAutoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
        }

        public int getStartUpTimeout() {
            return startUpTimeout;
        }

        public void setStartUpTimeout(int startUpTimeout) {
            this.startUpTimeout = startUpTimeout;
        }

        public void setEnv(List<Variable> variables) {
            this.env = variables;
        }

        public List<Variable> getEnv() {
            if (env == null) {
                env = new ArrayList<>();
            }
            return env;
        }

        public void setLabels(List<Label> labels) {
            this.labels = labels;
        }

        public List<Label> getLabels() {
            if (labels == null) {
                labels = new ArrayList<>();
            }
            return labels;
        }

        public List<Integer> getExposedPorts() {
            if (exposedPorts == null) {
                exposedPorts = new ArrayList<>();
            }
            return exposedPorts;
        }

        public void setExposedPorts(List<Integer> exposedPorts) {
            this.exposedPorts = exposedPorts;
        }

        public List<String> getPortBindings() {
            if (portBindings == null) {
                portBindings = new ArrayList<>();
            }
            return portBindings;
        }

        public void setPortBindings(List<String> portBindings) {
            this.portBindings = portBindings;
        }

        public List<VolumeMount> getVolumeMounts() {
            if (volumeMounts == null) {
                volumeMounts = new ArrayList<>();
            }
            return volumeMounts;
        }

        public void setVolumeMounts(List<VolumeMount> volumeMounts) {
            this.volumeMounts = volumeMounts;
        }
    }

    public static class LocalStack extends Container {

        protected String version;

        protected List<String> services;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getServices() {
            if (services == null) {
                services = new ArrayList<>();
            }
            return services;
        }

        public void setServices(List<String> services) {
            this.services = services;
        }

    }

    public static class MongoDB extends Container {

        protected String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class Kafka extends Container {

        protected String version;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class Redpanda extends Container {

        protected String version;

        public String getVersion() {
            return version;
        }

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

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDataSourceName() {
            return dataSourceName;
        }

        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public InitScript getInitScript() {
            return initScript;
        }

        public void setInitScript(InitScript initScript) {
            this.initScript = initScript;
        }

        public static class InitScript {

            protected String file;
            protected String value;

            public String getFile() {
                return file;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

        }
    }

    public static class VolumeMount {

        protected String file;
        protected String mountPath;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getMountPath() {
            return mountPath;
        }

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

        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

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

        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
