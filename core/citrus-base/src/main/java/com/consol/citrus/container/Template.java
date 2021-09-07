/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a previously defined block of test actions. Test cases can call
 * templates and reuse their functionality.
 *
 * Templates operate on test variables. While calling, the template caller can set these
 * variables as parameters.
 *
 * Nested test actions are executed in sequence.
 *
 * The template execution may affect existing variable values in the calling test case. So
 * variables may have different values in the test case after template execution. Therefore
 * users can create a local test context by setting globalContext to false. Templates then will
 * have no affect on the variables used in the test case.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class Template extends AbstractTestAction {

    /** List of actions to be executed */
    private final List<TestAction> actions;

    /** List of parameters to set before execution */
    private final Map<String, String> parameter;

    /** Should variables effect the global variables scope? */
    private final boolean globalContext;

    /**
     * Default constructor
     * @param builder
     */
    public Template(AbstractTemplateBuilder<? extends Template, ?> builder) {
        super(Optional.ofNullable(builder.templateName).orElse("template"), builder);

        this.actions = builder.actions;
        this.parameter = builder.parameter;
        this.globalContext = builder.globalContext;
    }

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(Template.class);

    @Override
    public void doExecute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Executing template '" + getName() + "' with " + actions.size() + " embedded actions");
        }

        TestContext innerContext;

        //decide whether to use global test context or not
        if (globalContext) {
            innerContext = context;
        } else {
            innerContext = new TestContext();
            innerContext.setFunctionRegistry(context.getFunctionRegistry());

            innerContext.setGlobalVariables(new GlobalVariables.Builder()
                    .variables(context.getGlobalVariables())
                    .build());
            innerContext.getVariables().putAll(context.getVariables());

            innerContext.setMessageStore(context.getMessageStore());
            innerContext.setMessageValidatorRegistry(context.getMessageValidatorRegistry());
            innerContext.setValidationMatcherRegistry(context.getValidationMatcherRegistry());
            innerContext.setTestListeners(context.getTestListeners());
            innerContext.setMessageListeners(context.getMessageListeners());
            innerContext.setMessageProcessors(context.getMessageProcessors());
            innerContext.setEndpointFactory(context.getEndpointFactory());
            innerContext.setNamespaceContextBuilder(context.getNamespaceContextBuilder());
            innerContext.setReferenceResolver(context.getReferenceResolver());
            innerContext.setTypeConverter(context.getTypeConverter());
            innerContext.setLogModifier(context.getLogModifier());
        }

        for (Entry<String, String> entry : parameter.entrySet()) {
            String param = entry.getKey();
            String paramValue = entry.getValue();

			paramValue = VariableUtils.replaceVariablesInString(paramValue, innerContext, false);
            if (context.getFunctionRegistry().isFunction(paramValue)) {
                paramValue = FunctionUtils.resolveFunction(paramValue, context);
            }

            if (log.isDebugEnabled()) {
                log.debug("Setting parameter for template " + param + "=" + paramValue);
            }

            innerContext.setVariable(param, paramValue);
        }

        for (TestAction action: actions) {
            action.execute(innerContext);
        }

        log.info("Template was executed successfully");
    }

    /**
     * Gets the parameter.
     * @return the parameter
     */
    public Map<String, String> getParameter() {
        return parameter;
    }

    /**
     * Gets the globalContext.
     * @return the globalContext
     */
    public boolean isGlobalContext() {
        return globalContext;
    }

    /**
     * Gets the actions.
     * @return the actions
     */
    public List<TestAction> getActions() {
        return actions;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTemplateBuilder<Template, Builder> {
        /**
         * Fluent API action building entry method used in Java DSL.
         * @param name
         * @return
         */
        public static Builder applyTemplate(String name) {
            Builder builder = new Builder();
            builder.templateName(name);
            return builder;
        }

        @Override
        public Template build() {
            onBuild();
            return new Template(this);
        }
    }

    /**
     * Action builder.
     */
    public static abstract class AbstractTemplateBuilder<T extends Template, B extends AbstractTemplateBuilder<T, B>> extends AbstractTestActionBuilder<T, B> {

        private String templateName;
        private List<TestAction> actions = new ArrayList<>();
        private Map<String, String> parameter = new LinkedHashMap<>();
        private boolean globalContext = true;

        private ReferenceResolver referenceResolver;

        public B templateName(String templateName) {
            this.templateName = templateName;
            return self;
        }

        /**
         * Boolean flag marking the template variables should also affect
         * variables in test case.
         * @param globalContext the globalContext to set
         */
        public B globalContext(boolean globalContext) {
            this.globalContext = globalContext;
            return self;
        }

        /**
         * Set parameter before execution.
         * @param parameters the parameter to set
         */
        public B parameters(Map<String, String> parameters) {
            this.parameter.putAll(parameters);
            return self;
        }

        /**
         * Set parameter before execution.
         * @param name
         * @param value
         */
        public B parameter(String name, String value) {
            this.parameter.put(name, value);
            return self;
        }

        /**
         * Adds test actions to the template.
         * @param actions
         * @return
         */
        public B actions(TestAction... actions) {
            return actions(Arrays.asList(actions));
        }

        /**
         * Adds test actions to the template.
         * @param actions
         * @return
         */
        public B actions(List<TestAction> actions) {
            this.actions = actions;
            return self;
        }

        /**
         * Sets the bean reference resolver for using endpoint names.
         * @param referenceResolver
         */
        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        protected void onBuild() {
            if (referenceResolver != null && templateName != null) {
                Template rootTemplate = referenceResolver.resolve(templateName, Template.class);
                globalContext(rootTemplate.isGlobalContext() && globalContext);
                actor(Optional.ofNullable(getActor()).orElse(rootTemplate.getActor()));
                parameters(Optional.ofNullable(rootTemplate.getParameter()).map(rootParams -> {
                    rootParams.putAll(parameter);
                    return rootParams;
                }).orElse(parameter));
                actions(rootTemplate.getActions());
            }
        }
    }
}
