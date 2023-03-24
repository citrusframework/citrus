/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.cucumber.container;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.citrusframework.container.Template;

/**
 * Step template executes a sequence of nested test actions. Template is configured with Gherkin syntax matching pattern and
 * optional parameter names for step arguments. By default this template is non global meaning that all parameter test variables
 * are only visible within the template.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class StepTemplate extends Template {

    /** The parameter names for this step */
    private final List<String> parameterNames;

    /** Gherkin given, when, then matching pattern */
    private final Pattern pattern;

    /**
     * Default constructor
     *
     * @param builder
     */
    public StepTemplate(Builder builder) {
        super(builder);

        this.parameterNames = builder.parameterNames;
        this.pattern = builder.pattern;
    }

    /**
     * Gets the value of the pattern property.
     *
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Gets the value of the parameterNames property.
     *
     * @return the parameterNames
     */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    /**
     * Provide parameter types for this step.
     * @return
     */
    public Type[] getParameterTypes() {
        Type[] types = new Type[parameterNames.size()];
        Arrays.fill(types, String.class);

        return types;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends Template.AbstractTemplateBuilder<StepTemplate, Builder> {

        private final List<String> parameterNames = new ArrayList<>();
        private Pattern pattern;

        public Builder parameterNames(String... parameterNames) {
            this.parameterNames.addAll(Arrays.asList(parameterNames));
            return this;
        }

        public Builder parameterNames(List<String> parameterNames) {
            this.parameterNames.addAll(parameterNames);
            return this;
        }

        public Builder pattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        public StepTemplate build() {
            onBuild();
            return new StepTemplate(this);
        }
    }
}
