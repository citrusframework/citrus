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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.actions.camel.CamelKubernetesIntegrationDeleteActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.jbang.JBangSupport.OK_EXIT_CODE;

/**
 * Delete kubernetes resources deployed from a Camel project or Camel integration with Camel JBang tooling.
 */
public class CamelKubernetesDeleteIntegrationAction extends AbstractCamelJBangAction {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CamelKubernetesDeleteIntegrationAction.class);

    /**
     * Camel integration resource
     */
    private final Resource integrationResource;

    /**
     * Camel integration name
     */
    private final String integrationName;

    /**
     * Camel Jbang cluster type target
     */
    private final String clusterType;

    /**
     * Project directory
     */
    private final String workingDir;

    /**
     * Kubernetes Namespace
     */
    private final String namespace;

    /**
     * Default constructor.
     */
    public CamelKubernetesDeleteIntegrationAction(Builder builder) {
        super("k8s-delete-integration", builder);

        this.integrationResource = builder.integrationResource;
        this.integrationName = builder.integrationName;
        this.clusterType = builder.clusterType;
        this.workingDir = builder.workingDir;
        this.namespace = builder.namespace;
    }

    @Override
    public void doExecute(TestContext context) {
        logger.info("Deleting integration deployed from a Camel Kubernetes project  ...");
        List<String> commandArgs = new ArrayList<>();
        if (integrationName != null) {
            commandArgs.add("--name");
            commandArgs.add(integrationName);
        }
        if (clusterType != null) {
            commandArgs.add("--cluster-type");
            commandArgs.add(clusterType);
        }
        if (workingDir != null) {
            commandArgs.add("--working-dir");
            commandArgs.add(workingDir);
        }
        if (namespace != null) {
            commandArgs.add("--namespace");
            commandArgs.add(namespace);
        }

        camelJBang().workingDir(integrationResource.getFile().toPath().toAbsolutePath().getParent());

        ProcessAndOutput pao = camelJBang().kubernetes().delete(integrationResource.getFile().getName(), commandArgs.toArray(String[]::new));
        logger.info(pao.getOutput());
        int exitValue = pao.getProcess().exitValue();
        if (exitValue != OK_EXIT_CODE) {
            throw new CitrusRuntimeException(String.format("Failed to delete Camel integration from Kubernetes - exit code %s", exitValue));
        }
    }

    public Resource getIntegrationResource() {
        return integrationResource;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public String getClusterType() {
        return clusterType;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<CamelKubernetesDeleteIntegrationAction, CamelKubernetesDeleteIntegrationAction.Builder>
            implements CamelKubernetesIntegrationDeleteActionBuilder<CamelKubernetesDeleteIntegrationAction, Builder> {

        private Resource integrationResource;
        private String integrationName;
        private String clusterType;
        private String workingDir;
        private String namespace;

        @Override
        public Builder integration(Resource resource) {
            this.integrationResource = resource;
            return this;
        }

        @Override
        public Builder integration(String name) {
            this.integrationName = name;
            return this;
        }

        @Override
        public Builder clusterType(String clusterType) {
            this.clusterType = clusterType;
            return this;
        }

        @Override
        public Builder workingDir(String dir) {
            this.workingDir = dir;
            return this;
        }

        @Override
        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        @Override
        public CamelKubernetesDeleteIntegrationAction doBuild() {
            return new CamelKubernetesDeleteIntegrationAction(this);
        }
    }
}
