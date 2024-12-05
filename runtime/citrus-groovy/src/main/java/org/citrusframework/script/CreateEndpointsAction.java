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

package org.citrusframework.script;

import java.io.IOException;
import java.nio.charset.Charset;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.groovy.dsl.configuration.endpoints.EndpointConfigurationScript;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEndpointsAction extends AbstractTestAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateEndpointsAction.class);

    /** Inline groovy script */
    private final String script;

    /** External script file resource path */
    private final String scriptResourcePath;

    /**
     * Default constructor.
     */
    public CreateEndpointsAction(Builder builder) {
        super("groovy-endpoints", builder);

        this.script = builder.script;
        this.scriptResourcePath = builder.scriptResourcePath;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            if (!StringUtils.hasText(script) && scriptResourcePath == null) {
                throw new CitrusRuntimeException("Neither inline script nor " +
                        "external script resource is defined. Unable to execute groovy script.");
            }

            String resolvedScript = StringUtils.hasText(script) ? script.trim() : FileUtils.readToString(FileUtils.getFileResource(scriptResourcePath, context));
            resolvedScript = context.replaceDynamicContentInString(resolvedScript.trim());

            new EndpointConfigurationScript(resolvedScript, context.getReferenceResolver()) {
                @Override
                protected void onCreate(Endpoint endpoint) {
                    logger.info("Creating endpoint from script configuration: %s".formatted(endpoint.getName()));
                }
            }.execute(context);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load endpoint configuration script", e);
        }
    }

    public String getScript() {
        return script;
    }

    public String getScriptResourcePath() {
        return scriptResourcePath;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<CreateEndpointsAction, Builder> {

        private String script;
        private String scriptResourcePath;

        /**
         * Sets the Groovy script to execute.
         * @param script
         * @return
         */
        public Builder script(String script) {
            this.script = script;
            return this;
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResource
         * @return
         */
        public Builder script(Resource scriptResource) {
            return script(scriptResource, FileUtils.getDefaultCharset());
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResource
         * @param charset
         * @return
         */
        public Builder script(Resource scriptResource, Charset charset) {
            try {
                this.script = FileUtils.readToString(scriptResource, charset);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource file", e);
            }
            return this;
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResourcePath
         * @return
         */
        public Builder scriptResourcePath(String scriptResourcePath) {
            this.scriptResourcePath = scriptResourcePath;
            return this;
        }

        @Override
        public CreateEndpointsAction build() {
            return new CreateEndpointsAction(this);
        }
    }
}
