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

package com.consol.citrus.cucumber.container;

import com.consol.citrus.container.Template;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private List<String> parameterNames = new ArrayList<>();

    /** Gherkin given, when, then matching pattern */
    private Pattern pattern;

    /**
     * Gets the value of the pattern property.
     *
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern property.
     *
     * @param pattern
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Sets the parameterNames property.
     *
     * @param parameterNames
     */
    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;
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
        for (int i = 0; i < types.length; i++) {
            types[i] = String.class;
        }

        return types;
    }
}
