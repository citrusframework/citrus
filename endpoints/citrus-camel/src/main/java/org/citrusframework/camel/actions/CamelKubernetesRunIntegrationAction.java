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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.camel.dsl.CamelSupport.camel;
import static org.citrusframework.jbang.JBangSupport.OK_EXIT_CODE;


/**
 * Run a Camel integration in kubernetes with Camel JBang tooling.
 */
public class CamelKubernetesRunIntegrationAction extends AbstractCamelJBangAction {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CamelKubernetesRunIntegrationAction.class);

    /** Name of Camel integration */
    private final String integrationName;

    /**
     * Camel integration resource
     */
    private final Resource integrationResource;

    /**
     * Camel integration project runtime
     */
    private final String runtime;

    /**
     * Camel Jbang image builder
     */
    private final String imageBuilder;

    /**
     * Camel Jbang image registry
     */
    private final String imageRegistry;

    /**
     * Camel Jbang cluster type target
     */
    private final String clusterType;

    /**
     * Camel Jbang command build properties
     */
    private final List<String> buildProperties;

    /**
     * Camel Jbang command properties
     */
    private final List<String> properties;

    /**
     * Camel Jbang command traits
     */
    private final List<String> traits;

    /**
     * Camel Jbang command arguments
     */
    private final List<String> args;

    private final boolean verbose;
    private final boolean autoRemoveResources;

    /**
     * Wait for integration pod to be in Running state
     */
    private final boolean waitForRunningState;

    /**
     * Default constructor.
     */
    public CamelKubernetesRunIntegrationAction(CamelKubernetesRunIntegrationAction.Builder builder) {
        super("kubernetes-run-integration", builder);

        this.integrationName = builder.integrationName;
        this.integrationResource = builder.integrationResource;
        this.runtime = builder.runtime;
        this.imageBuilder = builder.imageBuilder;
        this.imageRegistry = builder.imageRegistry;
        this.clusterType = builder.clusterType;
        this.buildProperties = builder.buildProperties;
        this.properties = builder.properties;
        this.traits = builder.traits;
        this.args = builder.args;
        this.verbose = builder.verbose;
        this.autoRemoveResources = builder.autoRemoveResources;
        this.waitForRunningState = builder.waitForRunningState;
    }

    @Override
    public void doExecute(TestContext context) {
        String name;
        if (StringUtils.hasText(integrationName)) {
            name = context.replaceDynamicContentInString(integrationName);
        } else {
            name = FileUtils.getBaseName(integrationResource.getFile().getName());
        }

        logger.info("Running Camel integration %s in Kubernetes ...".formatted(name));

        Path integrationToRun;
        if (integrationResource != null) {
            integrationToRun = integrationResource.getFile().toPath();
        } else {
            throw new CitrusRuntimeException("Missing Camel integration source code or file");
        }

        List<String> commandArgs = new ArrayList<>();
        if (runtime != null) {
            commandArgs.add("--runtime");
            commandArgs.add(runtime);
        }
        if (imageBuilder != null) {
            commandArgs.add("--image-builder");
            commandArgs.add(imageBuilder);
        }
        if (imageRegistry != null) {
            commandArgs.add("--image-registry");
            commandArgs.add(imageRegistry);
        }
        if (clusterType != null) {
            commandArgs.add("--cluster-type");
            commandArgs.add(clusterType);
        }

        if (buildProperties != null) {
            for (String property : buildProperties) {
                commandArgs.add("--build-property");
                commandArgs.add(property);
            }
        }

        if (properties != null) {
            for (String property : properties) {
                commandArgs.add("--property");
                commandArgs.add(property);
            }
        }

        if (traits != null) {
            for (String trait : traits) {
                commandArgs.add("--trait");
                commandArgs.add(trait);
            }
        }

        if (args != null) {
            commandArgs.addAll(args);
        }

        if (verbose) {
            commandArgs.add("--verbose=true");
        }

        camelJBang().workingDir(integrationToRun.toAbsolutePath().getParent());

        ProcessAndOutput pao = camelJBang().kubernetes().run(integrationResource.getFile().getName(), commandArgs.toArray(String[]::new));
        logger.info(pao.getOutput());
        int exitValue = pao.getProcess().exitValue();
        if (exitValue != OK_EXIT_CODE) {
            throw new CitrusRuntimeException(String.format("Failed to start Camel integration in Kubernetes - exit code %s", exitValue));
        }

        if (autoRemoveResources) {
            context.doFinally(camel()
                    .jbang()
                    .kubernetes()
                    .delete()
                    .integration(integrationResource)
                    .withReferenceResolver(context.getReferenceResolver()));
        }

        if (waitForRunningState) {
            //TODO: implement it!
        }
    }


    public Resource getIntegrationResource() {
        return integrationResource;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getImageBuilder() {
        return imageBuilder;
    }

    public String getImageRegistry() {
        return imageRegistry;
    }

    public List<String> getBuildProperties() {
        return buildProperties;
    }

    public List<String> getProperties() {
        return properties;
    }

    public List<String> getTraits() {
        return traits;
    }

    public List<String> getArgs() {
        return args;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelKubernetesRunIntegrationAction, CamelKubernetesRunIntegrationAction.Builder> {

        private String integrationName;
        private Resource integrationResource;

        private String runtime;
        private String imageBuilder;
        private String imageRegistry;
        private String clusterType;

        private final List<String> buildProperties = new ArrayList<>();
        private final List<String> properties = new ArrayList<>();
        private final List<String> traits = new ArrayList<>();
        private final List<String> args = new ArrayList<>();

        private boolean verbose = CamelJBangSettings.isVerbose();
        private boolean autoRemoveResources = CamelJBangSettings.isAutoRemoveResources();
        private boolean waitForRunningState = CamelJBangSettings.isWaitForRunningState();

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
         * Export given Camel integration resource.
         *
         * @param resource
         * @return
         */
        public Builder integration(Resource resource) {
            this.integrationResource = resource;
            return this;
        }

        /**
         * Define runtime.
         *
         * @param runtime
         * @return
         */
        public Builder runtime(String runtime) {
            this.runtime = runtime;
            return this;
        }

        /**
         * Define container image builder type.
         *
         * @param imageBuilder
         * @return
         */
        public Builder imageBuilder(String imageBuilder) {
            this.imageBuilder = imageBuilder;
            return this;
        }

        /**
         * Set container image registry.
         *
         * @param imageRegistry
         * @return
         */
        public Builder imageRegistry(String imageRegistry) {
            this.imageRegistry = imageRegistry;
            return this;
        }

        /**
         * Set cluster type target.
         *
         * @param clusterType
         * @return
         */
        public Builder clusterType(String clusterType) {
            this.clusterType = clusterType;
            return this;
        }

        /**
         * Adds a command build property.
         *
         * @param property
         * @return
         */
        public Builder withBuildProperty(String property) {
            this.buildProperties.add(property);
            return this;
        }

        /**
         * Adds command build properties.
         *
         * @param properties
         * @return
         */
        public Builder withBuildProperties(String... properties) {
            this.buildProperties.addAll(Arrays.asList(properties));
            return this;
        }

        /**
         * Adds a command property.
         *
         * @param property
         * @return
         */
        public Builder withProperty(String property) {
            this.properties.add(property);
            return this;
        }

        /**
         * Adds command properties.
         *
         * @param properties
         * @return
         */
        public Builder withProperties(String... properties) {
            this.properties.addAll(Arrays.asList(properties));
            return this;
        }

        /**
         * Adds a command trait.
         *
         * @param trait
         * @return
         */
        public Builder withTrait(String trait) {
            this.traits.add(trait);
            return this;
        }

        /**
         * Adds command traits.
         *
         * @param traits
         * @return
         */
        public Builder withTraits(String... traits) {
            this.traits.addAll(Arrays.asList(traits));
            return this;
        }

        /**
         * Adds a command argument.
         *
         * @param arg
         * @return
         */
        public Builder withArg(String arg) {
            this.args.add(arg);
            return this;
        }

        /**
         * Adds a command argument with name and value.
         *
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
         *
         * @param args
         * @return
         */
        public Builder withArgs(String... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        public Builder verbose(boolean enabled) {
            this.verbose = enabled;
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
        public CamelKubernetesRunIntegrationAction doBuild() {
            return new CamelKubernetesRunIntegrationAction(this);
        }
    }
}
