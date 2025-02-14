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
import java.util.List;

import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.AbstractCamelJBangAction;
import org.citrusframework.camel.actions.AddCamelPluginAction;
import org.citrusframework.camel.actions.CamelKubernetesDeleteAction;
import org.citrusframework.camel.actions.CamelKubernetesRunIntegrationAction;
import org.citrusframework.camel.actions.CamelKubernetesVerifyAction;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;

public class JBang implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

    private AbstractCamelJBangAction.Builder<?, ?> builder;

    private String camelVersion;
    private String kameletsVersion;

    public void setCamelVersion(String camelVersion) {
        this.camelVersion = camelVersion;
    }

    public String getCamelVersion() {
        return camelVersion;
    }

    public void setKameletsVersion(String kameletsVersion) {
        this.kameletsVersion = kameletsVersion;
    }

    public String getKameletsVersion() {
        return kameletsVersion;
    }

    public void setRun(RunIntegration run) {
        this.builder = run.getBuilder();
    }

    public void setStop(StopIntegration stop) {
        this.builder = stop.getBuilder();
    }

    public void setVerify(VerifyIntegration verify) {
        this.builder = verify.getBuilder();
    }

    public void setPlugin(Plugin plugin) {
        this.builder = plugin.getBuilder();
    }

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

        public void setAutoRemove(boolean enabled) {
            builder.autoRemove(enabled);
        }

        public void setWaitForRunningState(boolean enabled) {
            builder.waitForRunningState(enabled);
        }

        public void setArgs(List<String> args) {
            builder.withArgs(args.toArray(String[]::new));
        }

        public void setResources(List<String> resources) {
            resources.forEach(builder::addResource);
        }

        public void setDumpIntegrationOutput(boolean enabled) {
            builder.dumpIntegrationOutput(enabled);
        }

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

            public void setName(String integrationName) {
                this.name = integrationName;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public void setSources(String sourceCode) {
                this.sourceCode = sourceCode;
            }

            public Environment getEnvironment() {
                return this.environment;
            }

            public void setEnvironment(Environment environment) {
                this.environment = environment;
            }

            public SystemProperties getSystemProperties() {
                return this.systemProperties;
            }

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

                public void setVariables(List<Variable> variables) {
                    this.variables = variables;
                }

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

            public static class SystemProperties {

                protected String file;

                protected List<Property> properties;

                public List<Property> getProperties() {
                    if (properties == null) {
                        properties = new ArrayList<>();
                    }
                    return this.properties;
                }

                public void setProperties(List<Property> properties) {
                    this.properties = properties;
                }

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
        }

        @Override
        public CamelRunIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class StopIntegration implements CamelActionBuilderWrapper<CamelStopIntegrationAction.Builder> {

        private final CamelStopIntegrationAction.Builder builder = new CamelStopIntegrationAction.Builder();

        public void setIntegration(String integrationName) {
            builder.integrationName(integrationName);
        }

        @Override
        public CamelStopIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class VerifyIntegration implements CamelActionBuilderWrapper<CamelVerifyIntegrationAction.Builder> {

        private final CamelVerifyIntegrationAction.Builder builder = new CamelVerifyIntegrationAction.Builder();

        public void setIntegration(String integrationName) {
            builder.integrationName(integrationName);
        }

        public void setLogMessage(String logMessage) {
            builder.waitForLogMessage(logMessage);
        }

        public void setPhase(String phase) {
            builder.isInPhase(phase);
        }

        public void setPrintLogs(boolean printLogs) {
            builder.printLogs(printLogs);
        }

        public void setDelayBetweenAttempts(long delayBetweenAttempts) {
            builder.delayBetweenAttempts(delayBetweenAttempts);
        }

        public void setMaxAttempts(int maxAttempts) {
            builder.maxAttempts(maxAttempts);
        }

        public void setStopOnErrorStatus(boolean stopOnErrorStatus) {
            builder.stopOnErrorStatus(stopOnErrorStatus);
        }

        @Override
        public CamelVerifyIntegrationAction.Builder getBuilder() {
            return builder;
        }
    }

    public static class Plugin implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

        private AbstractCamelJBangAction.Builder<?, ?> builder;

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

            public void setName(String name) {
                this.name = name;
            }

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
            }

            public void setArgs(List<String> args) {
                this.args = args;
            }
        }
    }

    public static class Kubernetes implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

        private AbstractCamelJBangAction.Builder<?, ?> builder;

        @Override
        public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
            return builder;
        }

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

        public void setVerify(Verify verify) {
            CamelKubernetesVerifyAction.Builder builder = new CamelKubernetesVerifyAction.Builder();
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

        public void setDelete(Delete delete) {
            CamelKubernetesDeleteAction.Builder builder = new CamelKubernetesDeleteAction.Builder();
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

            public void setIntegration(Integration integration) {
                this.integration = integration;
            }

            public String getRuntime() {
                return runtime;
            }

            public void setRuntime(String runtime) {
                this.runtime = runtime;
            }

            public String getImageRegistry() {
                return imageRegistry;
            }

            public void setImageRegistry(String imageRegistry) {
                this.imageRegistry = imageRegistry;
            }

            public String getImageBuilder() {
                return imageBuilder;
            }

            public void setImageBuilder(String imageBuilder) {
                this.imageBuilder = imageBuilder;
            }

            public String getClusterType() {
                return clusterType;
            }

            public void setClusterType(String clusterType) {
                this.clusterType = clusterType;
            }

            public List<String> getBuildProperties() {
                if (buildProperties == null) {
                    buildProperties = new ArrayList<>();
                }
                return this.buildProperties;
            }

            public void setBuildProperties(List<String> buildProperties) {
                this.buildProperties = buildProperties;
            }

            public List<String> getProperties() {
                if (properties == null) {
                    properties = new ArrayList<>();
                }
                return this.properties;
            }

            public void setProperties(List<String> properties) {
                this.properties = properties;
            }

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

            public void setArgs(List<String> args) {
                this.args = args;
            }

            public boolean isVerbose() {
                return verbose;
            }

            public void setVerbose(boolean verbose) {
                this.verbose = verbose;
            }

            public boolean isAutoRemove() {
                return autoRemove;
            }

            public void setAutoRemove(boolean autoRemove) {
                this.autoRemove = autoRemove;
            }

            public String getArgLine() {
                return argLine;
            }

            public void setArgLine(String argLine) {
                this.argLine = argLine;
            }

            public boolean isWaitForRunningState() {
                return waitForRunningState;
            }

            public void setWaitForRunningState(boolean enabled) {
                this.waitForRunningState = enabled;
            }

            public static class Integration {
                protected String file;

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

            public void setIntegration(String integration) {
                this.integration = integration;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }

            public String getNamespace() {
                return namespace;
            }

            public void setNamespace(String namespace) {
                this.namespace = namespace;
            }

            public String getLogMessage() {
                return logMessage;
            }

            public void setLogMessage(String logMessage) {
                this.logMessage = logMessage;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public long getDelayBetweenAttempts() {
                return delayBetweenAttempts;
            }

            public void setDelayBetweenAttempts(long delayBetweenAttempts) {
                this.delayBetweenAttempts = delayBetweenAttempts;
            }

            public boolean isPrintLogs() {
                return printLogs;
            }

            public void setPrintLogs(boolean printLogs) {
                this.printLogs = printLogs;
            }


            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return this.args;
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

            public void setClusterType(String clusterType) {
                this.clusterType = clusterType;
            }

            public String getWorkingDir() {
                return workingDir;
            }

            public void setWorkingDir(String workingDir) {
                this.workingDir = workingDir;
            }

            public String getNamespace() {
                return namespace;
            }

            public void setNamespace(String namespace) {
                this.namespace = namespace;
            }

            public Integration getIntegration() {
                return integration;
            }

            public void setIntegration(Integration integration) {
                this.integration = integration;
            }


            public static class Integration {
                protected String file;
                protected String name;

                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }
            }
        }
    }

}
