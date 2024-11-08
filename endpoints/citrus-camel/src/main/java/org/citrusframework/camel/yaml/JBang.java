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

import org.citrusframework.camel.actions.AbstractCamelJBangAction;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;

public class JBang implements CamelActionBuilderWrapper<AbstractCamelJBangAction.Builder<?, ?>> {

    private String camelVersion;
    private String kameletsVersion;

    protected RunIntegration run;
    protected StopIntegration stop;
    protected VerifyIntegration verify;

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

    @Override
    public AbstractCamelJBangAction.Builder<?, ?> getBuilder() {
        AbstractCamelJBangAction.Builder<?, ?> builder;
        if (run != null) {
            builder = run.getBuilder();
        } else if (stop != null) {
            builder = stop.getBuilder();
        } else if (verify != null) {
            builder = verify.getBuilder();
        } else {
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
}
