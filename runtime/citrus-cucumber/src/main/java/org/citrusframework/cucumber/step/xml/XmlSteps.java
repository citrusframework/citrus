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

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.container.Template;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Step executes a XML template on test designer. The template is provided with step arguments as test template parameters.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class XmlSteps {

    @CitrusResource
    private TestCaseRunner runner;

    /**
     * Run template within designer.
     * @param stepTemplate
     * @param args
     */
    public void execute(StepTemplate stepTemplate, Object[] args) {
        Template.Builder templateBuilder = new Template.Builder()
            .name(stepTemplate.getName())
            .actions(stepTemplate.getActions())
            .actor(stepTemplate.getActor())
            .globalContext(stepTemplate.isGlobalContext());

        if (stepTemplate.getParameterNames().size() != args.length) {
            throw new CitrusRuntimeException(String.format("Step argument mismatch for template '%s', expected %s arguments but found %s",
                    stepTemplate.getName(), stepTemplate.getParameterNames().size(), args.length));
        }

        for (int i = 0; i < args.length; i++) {
            templateBuilder.parameter(stepTemplate.getParameterNames().get(i), args[i].toString());
        }

        if (runner != null) {
            runner.run(templateBuilder);
        }
    }
}
