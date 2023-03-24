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

package org.citrusframework.cucumber.step.xml;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.cucumber.container.StepTemplate;
import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

/**
 * Special step definition that runs a XML step definition template on execution.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class XmlStepDefinition implements StepDefinition {

    private final Lookup lookup;
    private final StepTemplate stepTemplate;

    public XmlStepDefinition(StepTemplate stepTemplate, Lookup lookup) {
        this.lookup = lookup;
        this.stepTemplate = stepTemplate;
    }

    @Override
    public String getLocation() {
        return stepTemplate.getName();
    }

    @Override
    public List<ParameterInfo> parameterInfos() {
        return Stream.of(stepTemplate.getParameterTypes()).map(XmlStepParameterInfo::new).collect(Collectors.toList());
    }

    @Override
    public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {
        lookup.getInstance(XmlSteps.class).execute(stepTemplate, args);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().equals(XmlSteps.class.getName());
    }

    @Override
    public String getPattern() {
        return stepTemplate.getPattern().pattern();
    }
}
