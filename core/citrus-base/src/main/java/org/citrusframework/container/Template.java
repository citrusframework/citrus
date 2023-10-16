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

package org.citrusframework.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.NoopTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.FunctionUtils;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.FileUtils;
import org.citrusframework.variable.VariableUtils;
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
    private final List<TestActionBuilder<?>> actions;

    private final String templateName;

    /** List of parameters to set before execution */
    private final Map<String, String> parameter;

    /** Should variables effect the global variables scope? */
    private final boolean globalContext;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Template.class);

    /**
     * Default constructor
     * @param builder
     */
    public Template(AbstractTemplateBuilder<? extends Template, ?> builder) {
        super(Optional.ofNullable(builder.templateName)
                .map(name  -> "template:" + name)
                .orElse("template"), builder);

        this.templateName = builder.templateName;
        this.actions = builder.actions;
        this.parameter = builder.parameter;
        this.globalContext = builder.globalContext;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing template '" + getName() + "' with " + actions.size() + " embedded actions");
        }

        TestContext innerContext;

        //decide whether to use global test context or not
        if (globalContext) {
            innerContext = context;
        } else {
            innerContext = TestContextFactory.copyOf(context);
        }

        for (Entry<String, String> entry : parameter.entrySet()) {
            String param = entry.getKey();
            String paramValue = entry.getValue();

			paramValue = VariableUtils.replaceVariablesInString(paramValue, innerContext, false);
            if (context.getFunctionRegistry().isFunction(paramValue)) {
                paramValue = FunctionUtils.resolveFunction(paramValue, context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Setting parameter for template " + param + "=" + paramValue);
            }

            innerContext.setVariable(param, paramValue);
        }

        for (TestActionBuilder<?> action: actions) {
            action.build().execute(innerContext);
        }

        logger.info("Template was executed successfully");
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
        return actions.stream().map(TestActionBuilder::build).collect(Collectors.toList());
    }

    /**
     * Gets the list of action builders.
     * @return the action builders.
     */
    public List<TestActionBuilder<?>> getActionBuilders() {
        return actions;
    }

    public String getTemplateName() {
        return templateName;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTemplateBuilder<Template, Builder> implements ReferenceResolverAware {

        private String filePath;

        private TemplateLoader loader;

        public static Builder applyTemplate() {
            return new Builder();
        }

        public Builder file(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder loader(TemplateLoader loader) {
            this.loader = loader;
            return this;
        }

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
            if (filePath != null) {
                if (loader == null) {
                    Optional<TemplateLoader> resolved = TemplateLoader.lookup(FileUtils.getFileExtension(filePath));
                    if (resolved.isPresent()) {
                        loader = resolved.get();
                    } else {
                        throw new CitrusRuntimeException(String.format("Failed to find proper template loader for file '%s'", filePath));
                    }
                }

                loader.setReferenceResolver(referenceResolver);
                Template local = loader.load(filePath);

                SimpleReferenceResolver temporaryReferenceResolver = new SimpleReferenceResolver();
                temporaryReferenceResolver.bind(local.getTemplateName(), local);

                withReferenceResolver(temporaryReferenceResolver);
                templateName(local.getTemplateName());
            }

            onBuild();
            return new Template(this);
        }
    }

    /**
     * Action builder.
     */
    public static abstract class AbstractTemplateBuilder<T extends Template, B extends AbstractTemplateBuilder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        private String templateName;
        private final List<TestActionBuilder<?>> actions = new ArrayList<>();
        private final Map<String, String> parameter = new LinkedHashMap<>();
        private boolean globalContext = true;

        protected ReferenceResolver referenceResolver;

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
            return actions(actions.stream()
                    .filter(action -> !(action instanceof NoopTestAction))
                    .map(action -> (TestActionBuilder<?>)() -> action)
                    .collect(Collectors.toList())
                    .toArray(new TestActionBuilder<?>[]{}));
        }

        /**
         * Adds test action builders to the template.
         * @param actions
         * @return
         */
        public B actions(TestActionBuilder<?>... actions) {
            for (int i = 0; i < actions.length; i++) {
                TestActionBuilder<?> current = actions[i];

                if (current.build() instanceof NoopTestAction) {
                    continue;
                }

                if (this.actions.size() == i) {
                    this.actions.add(current);
                } else if (!resolveActionBuilder(this.actions.get(i)).equals(resolveActionBuilder(current))) {
                    this.actions.add(i, current);
                }
            }
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

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        /**
         * Gets the list of test actions for this template.
         * @return
         */
        public List<TestActionBuilder<?>> getActions() {
            return actions;
        }

        protected void onBuild() {
            if (referenceResolver != null && templateName != null) {
                Template rootTemplate = referenceResolver.resolve(templateName, Template.class);
                globalContext(rootTemplate.isGlobalContext() && globalContext);
                actor(Optional.ofNullable(getActor()).orElseGet(rootTemplate::getActor));
                Map<String, String> mergedParameters = new LinkedHashMap<>();
                mergedParameters.putAll(rootTemplate.getParameter());
                mergedParameters.putAll(parameter);
                parameters(mergedParameters);
                actions(rootTemplate.getActionBuilders().toArray(TestActionBuilder[]::new));
            }
        }

        /**
         * Resolve action builder and takes care of delegating builders.
         * @param builder the builder maybe a delegating builder.
         * @return the builder itself or the delegate builder if this builder is a delegating builder.
         */
        private TestActionBuilder<?> resolveActionBuilder(TestActionBuilder<?> builder) {
            if (builder instanceof DelegatingTestActionBuilder) {
                return resolveActionBuilder(((DelegatingTestActionBuilder<?>) builder).getDelegate());
            }
            return builder;
        }
    }
}
