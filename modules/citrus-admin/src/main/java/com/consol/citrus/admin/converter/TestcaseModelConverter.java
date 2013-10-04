/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.converter;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.model.testcase.core.*;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class TestcaseModelConverter implements ObjectConverter<Testcase, TestCase> {

    @Override
    public Testcase convert(TestCase definition) {
        Testcase testModel = new Testcase();

        testModel.setName(definition.getName());
        testModel.setDescription(definition.getDescription());

        MetaInfoType metaInfoType = new MetaInfoType();
        metaInfoType.setAuthor(definition.getMetaInfo().getAuthor());
        metaInfoType.setStatus(definition.getMetaInfo().getStatus().name());
        testModel.setMetaInfo(metaInfoType);

        Variables variables = new Variables();
        for (Map.Entry<String, ?> variableEntry : definition.getVariableDefinitions().entrySet()) {
            Variables.Variable variable = new Variables.Variable();

            variable.setName(variableEntry.getKey());
            variable.setValue(variableEntry.getValue().toString());

            variables.getVariables().add(variable);
        }

        testModel.setVariables(variables);

        for (TestAction testAction : definition.getActions()) {
            Action action = new Action();
            action.setReference(testAction.getName());
            action.setDescription(testAction.getDescription());

            ActionListType actions = new ActionListType();
            actions.getActionsAndSendsAndReceives().add(action);

            testModel.setActions(actions);
        }

        return testModel;
    }
}
