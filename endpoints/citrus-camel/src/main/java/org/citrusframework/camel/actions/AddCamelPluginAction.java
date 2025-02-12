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
import java.util.Arrays;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install a specific plugin to a Camel JBang tooling.
 */
public class AddCamelPluginAction extends AbstractCamelJBangAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AddCamelPluginAction.class);

    /** Camel Jbang plugin name */
    private final String name;
    /** Camel Jbang command arguments */
    private final List<String> args;

    /**
     * Default constructor.
     */
    public AddCamelPluginAction(AddCamelPluginAction.Builder builder) {
        super("plugin-add", builder);
        this.name = builder.name;
        this.args = builder.args;
    }

    @Override
    public void doExecute(TestContext context) {
        String pluginName = context.replaceDynamicContentInString(name);
        List<String> installedPlugins = camelJBang().getPlugins();

        if (!installedPlugins.contains(pluginName)) {
            logger.info("Adding Camel plugin '%s' ...".formatted(pluginName));
            camelJBang().addPlugin(pluginName, context.resolveDynamicValuesInList(args).toArray(String[]::new));
            logger.info("Camel plugin '%s' successfully installed".formatted(pluginName));
        } else {
            logger.info("Camel plugin '%s' already installed".formatted(pluginName));
        }

    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelJBangAction.Builder<AddCamelPluginAction, AddCamelPluginAction.Builder> {
        private String name;
        private final List<String> args = new ArrayList<>();


        /**
         * Sets the plugin name.
         * @param name
         * @return
         */
        public Builder pluginName(String name) {
            this.name = name;
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

        @Override
        public AddCamelPluginAction doBuild() {
            return new AddCamelPluginAction(this);
        }
    }
}
