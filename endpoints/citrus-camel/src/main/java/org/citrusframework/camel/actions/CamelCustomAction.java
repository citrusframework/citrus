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

import org.citrusframework.actions.camel.CamelJBangCustomActionBuilder;
import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.citrusframework.camel.dsl.CamelSupport.camel;

/**
 * Runs given Camel (custom - parametrized) integration with Camel JBang tooling.
 */
public class CamelCustomAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelCustomAction.class);

    /** Name of Camel integration. More values are used in case more commands have to be used before resources. */
    private final String[] commands;

    /** ProcessName to be matched in camel ps. If null, first command is used. */
    private final String processName;

    /** Sets work dir, if is null, parent path of the first resource is used. */
    private final String workDir;

    /** Optional list of resource files to include */
    private final List<String> resourceFiles;

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
    public CamelCustomAction(Builder builder) {
        super("run-integration", builder);

        this.commands = builder.commands;
        this.resourceFiles = builder.resourceFiles;
        this.workDir = builder.workDir;
        this.args = builder.args;
        this.processName = builder.processName;
        this.envVars = builder.envVars;
        this.systemProperties = builder.systemProperties;
        this.autoRemoveResources = builder.autoRemoveResources;
        this.waitForRunningState = builder.waitForRunningState;
        this.dumpIntegrationOutput = builder.dumpIntegrationOutput;
    }

    @Override
    public void doExecute(TestContext context) {
        if(commands.length == 0) {
            throw new CitrusRuntimeException("Missing Camel integration name");
        }
        if(resourceFiles.isEmpty()) {
            throw new CitrusRuntimeException("Missing resource files");
        }
        String _workDir = workDir;
        if(_workDir == null) {
            File workDirFile = new File(resourceFiles.get(0));
            if(!workDirFile.exists()) {
                throw new CitrusRuntimeException("Missing work dir or full resource path");
            }
            _workDir = workDirFile.getParent();
        }

        List<String> names = Arrays.stream(commands).map(context::replaceDynamicContentInString).collect(Collectors.toList());
        String command = names.get(0);
        List<String> subNames = names.subList(1, names.size());

        logger.info("Starting Camel integration '%s' ...".formatted(names.get(0)));

        camelJBang().dumpIntegrationOutput(dumpIntegrationOutput);
        camelJBang().withEnvs(context.resolveDynamicValuesInMap(envVars));
        camelJBang().withSystemProperties(context.resolveDynamicValuesInMap(systemProperties));
        camelJBang().workingDir(Path.of(_workDir));

        ProcessAndOutput pao = camelJBang().custom(command,
                _workDir,
                subNames,
                resourceFiles,
                context.resolveDynamicValuesInList(args).toArray(String[]::new));

        var _processName = processName == null ? commands[0] : processName;
        verifyProcessIsAlive(pao, _processName);

        Long pid = pao.getProcessId();

        context.setVariable("%s:pid".formatted(_processName), pid);
        context.setVariable("%s:process:%d".formatted(_processName, pid), pao);

        logger.info("Started Camel integration '%s' (%s)".formatted(_processName, pid));

        if (autoRemoveResources) {
            context.doFinally(camel()
                    .jbang()
                    .stop()
                    .integration(_processName));
        }

        logger.info("Waiting for the Camel integration '%s' (%s) to be running ...".formatted(_processName, pid));

        if (waitForRunningState) {
            new CamelVerifyIntegrationAction.Builder()
                    .integrationName(_processName)
                    .isRunning()
                    .build()
                    .execute(context);
        }
    }

    private static void verifyProcessIsAlive(ProcessAndOutput pao, String name) {
        if (!pao.getProcess().isAlive()) {
            logger.info("Failed to start Camel integration '%s'".formatted(name));
            logger.info(pao.getOutput());

            throw new CitrusRuntimeException(String.format("Failed to start Camel integration - exit code %s", pao.getProcess().exitValue()));
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelCustomAction, Builder>
            implements CamelJBangCustomActionBuilder<CamelCustomAction, Builder> {

        private String processName;
        private String[] commands = new String[0];
        private Resource integrationResource;
        private final List<String> resourceFiles = new ArrayList<>();
        private String workDir;

        private final List<String> args = new ArrayList<>();
        private final Map<String, String> envVars = new HashMap<>();
        private Resource envVarsFile;
        private final Map<String, String> systemProperties = new HashMap<>();
        private Resource systemPropertiesFile;

        private boolean autoRemoveResources = CamelJBangSettings.isAutoRemoveResources();
        private boolean waitForRunningState = CamelJBangSettings.isWaitForRunningState();
        private boolean dumpIntegrationOutput = CamelJBangSettings.isDumpIntegrationOutput();

        @Override
        public Builder commands(String... commands) {
            this.commands = commands;
            return this;
        }

        @Override
        public Builder processName(String processNane) {
            this.processName = processNane;
            return this;
        }

        @Override
        public Builder addResource(Resource resource) {
            this.resourceFiles.add(resource.getFile().getAbsolutePath());
            return this;
        }


        @Override
        public Builder addResources(Resource... resources) {
            Arrays.asList(resources).forEach(this::addResource);
            return this;
        }

        @Override
        public Builder addResource(String resourcePath) {
            this.resourceFiles.add(resourcePath);
            return this;
        }

        @Override
        public Builder addResources(String... resources) {
            Arrays.asList(resources).forEach(this::addResource);
            return this;
        }

        @Override
        public Builder workDir(String workDir) {
            this.workDir = workDir;
            return this;
        }

        @Override
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        @Override
        public Builder withArg(String name, String value) {
            this.args.add(name);
            this.args.add(value);
            return this;
        }

        @Override
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        @Override
        public Builder withEnv(String key, String value) {
            this.envVars.put(key, value);
            return this;
        }

        @Override
        public Builder withEnvs(Map<String, String> envVars) {
            this.envVars.putAll(envVars);
            return this;
        }

        @Override
        public Builder withEnvs(Resource envVarsFile) {
            this.envVarsFile = envVarsFile;
            return this;
        }

        @Override
        public Builder withSystemProperty(String key, String value) {
            this.systemProperties.put(key, value);
            return this;
        }

        @Override
        public Builder withSystemProperties(Map<String, String> systemProperties) {
            this.systemProperties.putAll(systemProperties);
            return this;
        }

        @Override
        public Builder withSystemProperties(Resource systemPropertiesFile) {
            this.systemPropertiesFile = systemPropertiesFile;
            return this;
        }

        @Override
        public Builder dumpIntegrationOutput(boolean enabled) {
            this.dumpIntegrationOutput = enabled;
            return this;
        }

        @Override
        public Builder autoRemove(boolean enabled) {
            this.autoRemoveResources = enabled;
            return this;
        }

        @Override
        public Builder waitForRunningState(boolean enabled) {
            this.waitForRunningState = enabled;
            return this;
        }

        @Override
        public CamelCustomAction doBuild() {
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

            return new CamelCustomAction(this);
        }

    }
}
