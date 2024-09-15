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

    /** Source code to run as a Camel integration */
    private final String sourceCode;

    /**
     * Default constructor.
     */
    public CamelRunIntegrationAction(Builder builder) {
        super("run-integration", builder);

        this.integrationName = builder.integrationName;
        this.integrationResource = builder.integrationResource;
        this.sourceCode = builder.sourceCode;
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

            ProcessAndOutput pao = camelJBang().run(name, integrationToRun);

            if (!pao.getProcess().isAlive()) {
                logger.info("Failed to start Camel integration '%s'".formatted(name));
                logger.info(pao.getOutput());

                throw new CitrusRuntimeException(String.format("Failed to start Camel integration - exit code %s", pao.getProcess().exitValue()));
            }

            Long pid = pao.getProcessId(integrationToRun.getFileName().toString());
            context.setVariable(name + ":pid", pid);
            context.setVariable(name + ":process:" + pid, pao);

            logger.info("Started Camel integration '%s'".formatted(name));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create temporary file from Camel integration");
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

        @Override
        public CamelRunIntegrationAction build() {
            return new CamelRunIntegrationAction(this);
        }
    }
}
