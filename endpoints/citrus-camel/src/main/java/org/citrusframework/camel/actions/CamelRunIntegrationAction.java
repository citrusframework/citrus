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

package org.citrusframework.camel.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.IsJsonPredicate;
import org.citrusframework.util.IsXmlPredicate;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.camel.dsl.CamelSupport.camel;

/**
 * Runs given Camel integration with Camel JBang tooling.
 */
public class CamelRunIntegrationAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelRunIntegrationAction.class);

    /** Name of Camel integration */
    private final String integrationName;

    /** Camel integration resource */
    private final Resource integrationResource;

    /** Optional list of resource files to include */
    private final List<String> resourceFiles;

    /** Source code to run as a Camel integration */
    private final String sourceCode;

    /** Camel Jbang command arguments */
    private final List<String> args;

    /** Environment variables set on the Camel JBang process */
    private final Map<String, String> envVars;

    /** System properties set on the Camel JBang process */
    private final Map<String, String> systemProperties;

    private final boolean autoRemoveResources;

    private final boolean waitForRunningState;
    private final boolean dumpIntegrationOutput;

    /**
     * Default constructor.
     */
    public CamelRunIntegrationAction(Builder builder) {
        super("run-integration", builder);

        this.integrationName = builder.integrationName;
        this.integrationResource = builder.integrationResource;
        this.resourceFiles = builder.resourceFiles;
        this.sourceCode = builder.sourceCode;
        this.args = builder.args;
        this.envVars = builder.envVars;
        this.systemProperties = builder.systemProperties;
        this.autoRemoveResources = builder.autoRemoveResources;
        this.waitForRunningState = builder.waitForRunningState;
        this.dumpIntegrationOutput = builder.dumpIntegrationOutput;
    }

    @Override
    public void doExecute(TestContext context) {
        String name = context.replaceDynamicContentInString(integrationName);

        try {
            logger.info("Starting Camel integration '%s' ...".formatted(name));

            Path integrationToRun;
            if (StringUtils.hasText(sourceCode)) {
                Path workDir = CamelJBangSettings.getWorkDir();
                Files.createDirectories(workDir);
                integrationToRun = workDir.resolve(String.format("i-%s.%s", name, getFileExt(sourceCode)));
                Files.writeString(integrationToRun, sourceCode,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } else if (integrationResource != null) {
                integrationToRun = integrationResource.getFile().toPath();
            } else {
                throw new CitrusRuntimeException("Missing Camel integration source code or file");
            }

            camelJBang().dumpIntegrationOutput(dumpIntegrationOutput);
            camelJBang().camelApp().withEnvs(context.resolveDynamicValuesInMap(envVars));
            camelJBang().camelApp().withSystemProperties(context.resolveDynamicValuesInMap(systemProperties));

            ProcessAndOutput pao = camelJBang().run(name, integrationToRun, resourceFiles, args.toArray(String[]::new));

            verifyProcessIsAlive(pao, name);

            pao.setApp(integrationToRun.getFileName().toString());
            Long pid = pao.getProcessId();

            context.setVariable(name + ":pid", pid);
            context.setVariable(name + ":process:" + pid, pao);

            logger.info("Started Camel integration '%s' (%s)".formatted(name, pid));

            if (autoRemoveResources) {
                context.doFinally(camel()
                        .jbang()
                        .stop(name));
            }

            logger.info("Waiting for the Camel integration '%s' (%s) to be running ...".formatted(name, pid));

            if (waitForRunningState) {
                new CamelVerifyIntegrationAction.Builder()
                        .integrationName(name)
                        .isRunning()
                        .build()
                        .execute(context);
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create temporary file from Camel integration");
        }
    }

    private static void verifyProcessIsAlive(ProcessAndOutput pao, String name) {
        if (!pao.getProcess().isAlive()) {
            logger.info("Failed to start Camel integration '%s'".formatted(name));
            logger.info(pao.getOutput());

            throw new CitrusRuntimeException(String.format("Failed to start Camel integration - exit code %s", pao.getProcess().exitValue()));
        }
    }

    private String getFileExt(String sourceCode) {
        if (IsXmlPredicate.getInstance().test(sourceCode)) {
            return "xml";
        } else if (IsJsonPredicate.getInstance().test(sourceCode)) {
            return "json";
        } else if (sourceCode.contains("static void main(")) {
            return "java";
        } else if (sourceCode.contains("- from:") || sourceCode.contains("- route:") ||
                sourceCode.contains("- routeConfiguration:") || sourceCode.contains("- rest:") || sourceCode.contains("- beans:")) {
            return "yaml";
        } else if (sourceCode.contains("kind: Kamelet") || sourceCode.contains("kind: KameletBinding") ||
                sourceCode.contains("kind: Pipe") || sourceCode.contains("kind: Integration")) {
            return "yaml";
        } else {
            return "groovy";
        }
    }

    public String getIntegrationName() {
        return integrationName;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelRunIntegrationAction, Builder> {

        private String sourceCode;
        private String integrationName = "route";
        private Resource integrationResource;
        private final List<String> resourceFiles = new ArrayList<>();

        private final List<String> args = new ArrayList<>();
        private final Map<String, String> envVars = new HashMap<>();
        private Resource envVarsFile;
        private final Map<String, String> systemProperties = new HashMap<>();
        private Resource systemPropertiesFile;

        private boolean autoRemoveResources = CamelJBangSettings.isAutoRemoveResources();
        private boolean waitForRunningState = CamelJBangSettings.isWaitForRunningState();
        private boolean dumpIntegrationOutput = CamelJBangSettings.isDumpIntegrationOutput();

        /**
         * Runs Camel integration from given source code.
         * @param sourceCode
         * @return
         */
        public Builder integration(String sourceCode) {
            this.sourceCode = sourceCode;
            return this;
        }

        /**
         * Runs given Camel integration resource.
         * @param resource
         * @return
         */
        public Builder integration(Resource resource) {
            this.integrationResource = resource;
            if (integrationName == null) {
                this.integrationName = FileUtils.getBaseName(FileUtils.getFileName(resource.getLocation()));
            }
            return this;
        }

        /**
         * Add resource file to the integration run.
         * @param resource
         * @return
         */
        public Builder addResource(Resource resource) {
            this.resourceFiles.add(resource.getFile().getAbsolutePath());
            return this;
        }

        /**
         * Construct resource from given path and add file as resource to the integration run.
         * @param resourcePath
         * @return
         */
        public Builder addResource(String resourcePath) {
            this.resourceFiles.add(resourcePath);
            return this;
        }

        /**
         * Adds route using one of the supported languages XML or Groovy.
         * @param name
         * @param sourceCode
         * @return
         */
        public Builder integration(String name, String sourceCode) {
            this.integrationName = name;
            this.sourceCode = sourceCode;
            return this;
        }

        /**
         * Sets the integration name.
         * @param name
         * @return
         */
        public Builder integrationName(String name) {
            this.integrationName = name;
            return this;
        }

        /**
         * Adds a command argument.
         * @param arg
         * @return
         */
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        /**
         * Adds a command argument with name and value.
         * @param name
         * @param value
         * @return
         */
        public Builder withArg(String name, String value) {
            this.args.add(name);
            this.args.add(value);
            return this;
        }

        /**
         * Adds command arguments.
         * @param args
         * @return
         */
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        /**
         * Adds an environment variable.
         * @param key
         * @param value
         * @return
         */
        public Builder withEnv(String key, String value) {
            this.envVars.put(key, value);
            return this;
        }

        /**
         * Adds environment variables.
         * @param envVars
         * @return
         */
        public Builder withEnvs(Map<String, String> envVars) {
            this.envVars.putAll(envVars);
            return this;
        }

        /**
         * Adds environment variables from given file resource.
         * @param envVarsFile
         * @return
         */
        public Builder withEnvs(Resource envVarsFile) {
            this.envVarsFile = envVarsFile;
            return this;
        }

        /**
         * Adds a system properties.
         * @param key
         * @param value
         * @return
         */
        public Builder withSystemProperty(String key, String value) {
            this.systemProperties.put(key, value);
            return this;
        }

        /**
         * Adds system properties.
         * @param systemProperties
         * @return
         */
        public Builder withSystemProperties(Map<String, String> systemProperties) {
            this.systemProperties.putAll(systemProperties);
            return this;
        }

        /**
         * Adds system properties from given file resource.
         * @param systemPropertiesFile
         * @return
         */
        public Builder withSystemProperties(Resource systemPropertiesFile) {
            this.systemPropertiesFile = systemPropertiesFile;
            return this;
        }

        public Builder dumpIntegrationOutput(boolean enabled) {
            this.dumpIntegrationOutput = enabled;
            return this;
        }

        public Builder autoRemove(boolean enabled) {
            this.autoRemoveResources = enabled;
            return this;
        }

        public Builder waitForRunningState(boolean enabled) {
            this.waitForRunningState = enabled;
            return this;
        }

        @Override
        public CamelRunIntegrationAction build() {
            if (systemPropertiesFile != null) {
                Properties props = new Properties();
                try {
                    props.load(systemPropertiesFile.getInputStream());
                    props.forEach((k, v) -> withSystemProperty(k.toString(), v.toString()));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read properties file", e);
                }
            }

            if (envVarsFile != null) {
                Properties props = new Properties();
                try {
                    props.load(envVarsFile.getInputStream());
                    props.forEach((k, v) -> withEnv(k.toString(), v.toString()));
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read properties file", e);
                }
            }

            return new CamelRunIntegrationAction(this);
        }

    }
}
