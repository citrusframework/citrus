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

import org.citrusframework.actions.camel.CamelCliPluginDeleteActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete a specific plugin from a Camel CLI tooling.
 */
public class DeleteCamelPluginAction extends AbstractCamelCliAction {

    private static final Logger logger = LoggerFactory.getLogger(DeleteCamelPluginAction.class);

    private final String name;

    public DeleteCamelPluginAction(Builder builder) {
        super("plugin-delete", builder);
        this.name = builder.name;
    }

    @Override
    public void doExecute(TestContext context) {
        String pluginName = context.replaceDynamicContentInString(name);
        logger.info("Deleting Camel plugin '{}' ...", pluginName);
        camelCli().deletePlugin(pluginName);
        logger.info("Camel plugin '{}' successfully deleted", pluginName);
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelCliAction.Builder<DeleteCamelPluginAction, Builder>
            implements CamelCliPluginDeleteActionBuilder<DeleteCamelPluginAction, Builder> {

        private String name;

        @Override
        public Builder pluginName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public DeleteCamelPluginAction doBuild() {
            return new DeleteCamelPluginAction(this);
        }
    }
}
