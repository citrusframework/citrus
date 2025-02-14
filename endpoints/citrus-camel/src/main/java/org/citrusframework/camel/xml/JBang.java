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

package org.citrusframework.camel.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.jbang.CamelJBangSettings;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "plugin",
        "kubernetes",
        "run",
        "stop",
        "verify",
})
public class JBang {

    @XmlAttribute
    private String camelVersion;

    @XmlAttribute
    private String kameletsVersion;

    @XmlElement
    protected RunIntegration run;

    @XmlElement
    protected StopIntegration stop;

    @XmlElement
    protected VerifyIntegration verify;

    @XmlElement
    protected Plugin plugin;

    @XmlElement
    protected Kubernetes kubernetes;

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
        this.run = run;
    }

    public RunIntegration getRun() {
        return run;
    }

    public void setStop(StopIntegration stop) {
        this.stop = stop;
    }

    public StopIntegration getStop() {
        return stop;
    }

    public void setVerify(VerifyIntegration verify) {
        this.verify = verify;
    }

    public VerifyIntegration getVerify() {
        return verify;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Kubernetes getKubernetes() {
        return kubernetes;
    }

    public void setKubernetes(Kubernetes kubernetes) {
        this.kubernetes = kubernetes;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "args",
            "integration",
            "resources"
    })
    public static class RunIntegration {

        @XmlElement(required = true)
        private Integration integration;

        @XmlAttribute(name = "auto-remove")
        protected boolean autoRemove;

        @XmlAttribute(name = "args")
        protected String argLine;

        @XmlAttribute(name = "wait-for-running-state")
        protected boolean waitForRunningState = CamelJBangSettings.isWaitForRunningState();

        @XmlAttribute(name = "dump-integration-output")
        protected boolean dumpIntegrationOutput = CamelJBangSettings.isDumpIntegrationOutput();

        @XmlElement
        protected Args args;

        @XmlElement
        protected Resources resources;

        public Integration getIntegration() {
            return integration;
        }

        public void setIntegration(Integration integration) {
            this.integration = integration;
        }

        public boolean isAutoRemove() {
            return autoRemove;
        }

        public void setAutoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
        }

        public boolean isWaitForRunningState() {
            return waitForRunningState;
        }

        public void setWaitForRunningState(boolean waitForRunningState) {
            this.waitForRunningState = waitForRunningState;
        }

        public boolean isDumpIntegrationOutput() {
            return dumpIntegrationOutput;
        }

        public void setDumpIntegrationOutput(boolean dumpIntegrationOutput) {
            this.dumpIntegrationOutput = dumpIntegrationOutput;
        }

        public void setResources(Resources resources) {
            this.resources = resources;
        }

        public Resources getResources() {
            return resources;
        }

        public String getArgLine() {
            return argLine;
        }

        public void setArgLine(String argLine) {
            this.argLine = argLine;
        }

        public Args getArgs() {
            return args;
        }

        public void setArgs(Args args) {
            this.args = args;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "args",
        })
        public static class Args {

            @XmlElement(name = "argument")
            protected List<String> args;

            public List<String> getArgs() {
                if (args == null) {
                    args = new ArrayList<>();
                }
                return args;
            }

            public void setArgs(List<String> args) {
                this.args = args;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "resources",
        })
        public static class Resources {

            @XmlElement(name = "resource")
            protected List<String> resources;

            public List<String> getResources() {
                if (resources == null) {
                    resources = new ArrayList<>();
                }
                return resources;
            }

            public void setResources(List<String> resources) {
                this.resources = resources;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "environment",
                "systemProperties",
                "source"
        })
        public static class Integration {
            @XmlAttribute
            protected String name;

            @XmlAttribute
            protected String file;

            @XmlElement
            protected String source;

            @XmlElement
            protected Environment environment;

            @XmlElement(name = "system-properties")
            protected SystemProperties systemProperties;

            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public String getFile() {
                return file;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public String getSource() {
                return source;
            }

            public Environment getEnvironment() {
                return environment;
            }

            public void setEnvironment(Environment environment) {
                this.environment = environment;
            }

            public SystemProperties getSystemProperties() {
                return systemProperties;
            }

            public void setSystemProperties(SystemProperties systemProperties) {
                this.systemProperties = systemProperties;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "variables",
            })
            public static class Environment {

                @XmlAttribute
                protected String file;

                @XmlElement(name = "variable")
                protected List<Variable> variables;

                public List<Variable> getVariables() {
                    if (variables == null) {
                        variables = new ArrayList<>();
                    }
                    return this.variables;
                }

                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Variable {

                    @XmlAttribute(name = "name", required = true)
                    protected String name;
                    @XmlAttribute(name = "value")
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

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "properties",
            })
            public static class SystemProperties {

                @XmlAttribute
                protected String file;

                @XmlElement(name = "property")
                protected List<Property> properties;

                public List<Property> getProperties() {
                    if (properties == null) {
                        properties = new ArrayList<>();
                    }
                    return this.properties;
                }

                public void setFile(String file) {
                    this.file = file;
                }

                public String getFile() {
                    return file;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Property {

                    @XmlAttribute(name = "name", required = true)
                    protected String name;
                    @XmlAttribute(name = "value")
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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class StopIntegration {
        @XmlAttribute
        protected String integration;

        public void setIntegration(String integration) {
            this.integration = integration;
        }

        public String getIntegration() {
            return integration;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class VerifyIntegration {
        @XmlAttribute
        protected String integration;

        @XmlAttribute(name = "log-message")
        private String logMessage;

        @XmlAttribute(name = "max-attempts")
        private int maxAttempts = CamelSettings.getMaxAttempts();
        @XmlAttribute(name = "delay-between-attempts")
        private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

        @XmlAttribute
        private String phase = "Running";
        @XmlAttribute(name = "print-logs")
        private boolean printLogs = CamelSettings.isPrintLogs();

        @XmlAttribute(name = "stop-on-error-status")
        private boolean stopOnErrorStatus = true;

        public void setIntegration(String integration) {
            this.integration = integration;
        }

        public String getIntegration() {
            return integration;
        }

        public void setLogMessage(String logMessage) {
            this.logMessage = logMessage;
        }

        public String getLogMessage() {
            return logMessage;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public String getPhase() {
            return phase;
        }

        public void setPrintLogs(boolean printLogs) {
            this.printLogs = printLogs;
        }

        public boolean isPrintLogs() {
            return printLogs;
        }

        public void setDelayBetweenAttempts(long delayBetweenAttempts) {
            this.delayBetweenAttempts = delayBetweenAttempts;
        }

        public long getDelayBetweenAttempts() {
            return delayBetweenAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setStopOnErrorStatus(boolean stopOnErrorStatus) {
            this.stopOnErrorStatus = stopOnErrorStatus;
        }

        public boolean isStopOnErrorStatus() {
            return stopOnErrorStatus;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "add"
    })
    public static class Plugin {

        @XmlElement
        protected Add add;

        public void setAdd(Add add) {
            this.add = add;
        }

        public Add getAdd() {
            return this.add;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Add {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "args")
            protected String argLine;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getArgLine() {
                return argLine;
            }

            public void setArgLine(String argLine) {
                this.argLine = argLine;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "run",
            "verify",
            "delete"
    })
    public static class Kubernetes {

        @XmlElement(name = "run")
        protected Run run;

        @XmlElement(name = "delete")
        protected Delete delete;

        @XmlElement(name = "verify")
        protected Verify verify;

        public Run getRun() {
            return run;
        }

        public void setRun(Run run) {
            this.run = run;
        }

        public Delete getDelete() {
            return delete;
        }

        public void setDelete(Delete delete) {
            this.delete = delete;
        }

        public Verify getVerify() {
            return verify;
        }

        public void setVerify(Verify verify) {
            this.verify = verify;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Run {

            @XmlElement(required = true)
            private Integration integration;

            @XmlAttribute
            protected String runtime;

            @XmlAttribute(name = "image-registry")
            protected String imageRegistry;

            @XmlAttribute(name = "image-builder")
            protected String imageBuilder;

            @XmlAttribute(name = "cluster-type")
            protected String clusterType;

            @XmlElement(name = "build-properties")
            protected Properties buildProperties;


            @XmlElement
            protected Properties properties;

            @XmlElement
            protected Traits traits;

            @XmlElement
            protected Args args;

            @XmlAttribute
            protected boolean verbose;

            @XmlAttribute(name = "auto-remove")
            protected boolean autoRemove;

            @XmlAttribute(name = "args")
            protected String argLine;

            @XmlAttribute(name = "wait-for-running-state")
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

            public Properties getBuildProperties() {
                return buildProperties;
            }

            public void setBuildProperties(Properties buildProperties) {
                this.buildProperties = buildProperties;
            }

            public Properties getProperties() {
                return properties;
            }

            public void setProperties(Properties properties) {
                this.properties = properties;
            }

            public Traits getTraits() {
                return traits;
            }

            public void setTraits(Traits traits) {
                this.traits = traits;
            }

            public Args getArgs() {
                return args;
            }

            public void setArgs(Args args) {
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

            public void setWaitForRunningState(boolean waitForRunningState) {
                this.waitForRunningState = waitForRunningState;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Integration {
                @XmlAttribute
                protected String file;

                public String getFile() {
                    return file;
                }

                public void setFile(String file) {
                    this.file = file;
                }
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Properties {

                @XmlElement(name = "property")
                protected List<Property> properties;

                public List<Property> getProperties() {
                    if (properties == null) {
                        properties = new ArrayList<>();
                    }
                    return this.properties;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Property {

                    @XmlAttribute(name = "name", required = true)
                    protected String name;
                    @XmlAttribute(name = "value")
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

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Traits {

                @XmlElement(name = "trait")
                protected List<Trait> traits;

                public List<Trait> getTraits() {
                    if (traits == null) {
                        traits = new ArrayList<>();
                    }
                    return this.traits;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class Trait {

                    @XmlAttribute(name = "name", required = true)
                    protected String name;
                    @XmlAttribute(name = "value")
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

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "args",
            })
            public static class Args {

                @XmlElement(name = "argument")
                protected List<String> args;

                public List<String> getArgs() {
                    if (args == null) {
                        args = new ArrayList<>();
                    }
                    return args;
                }

                public void setArgs(List<String> args) {
                    this.args = args;
                }
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Verify {

            @XmlAttribute
            protected String integration;
            @XmlAttribute
            protected String label;
            @XmlAttribute
            private String namespace;

            @XmlAttribute(name = "log-message")
            private String logMessage;

            @XmlAttribute(name = "max-attempts")
            private int maxAttempts = CamelSettings.getMaxAttempts();
            @XmlAttribute(name = "delay-between-attempts")
            private long delayBetweenAttempts = CamelSettings.getDelayBetweenAttempts();

            @XmlAttribute(name = "print-logs")
            private boolean printLogs = CamelSettings.isPrintLogs();

            @XmlElement(name = "args")
            protected Args args;

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

            public Args getArgs() {
                return args;
            }

            public void setArgs(Args args) {
                this.args = args;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                    "args",
            })
            public static class Args {

                @XmlElement(name = "argument")
                protected List<String> args;

                public List<String> getArgs() {
                    if (args == null) {
                        args = new ArrayList<>();
                    }
                    return args;
                }

                public void setArgs(List<String> args) {
                    this.args = args;
                }
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Delete {

            @XmlElement(required = true)
            private Integration integration;

            @XmlAttribute(name = "cluster-type")
            protected String clusterType;
            @XmlAttribute(name = "working-dir")
            protected String workingDir;
            @XmlAttribute(name = "namespace")
            protected String namespace;

            public Integration getIntegration() {
                return integration;
            }

            public void setIntegration(Integration integration) {
                this.integration = integration;
            }

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

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Integration {
                @XmlAttribute
                protected String file;

                @XmlAttribute
                protected String name;

                public String getFile() {
                    return file;
                }

                public void setFile(String file) {
                    this.file = file;
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
