/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.dsl.definition.AbstractActionDefinition;
import com.consol.citrus.dsl.definition.CreateVariablesActionDefinition;
import org.springframework.context.ApplicationContext;

import java.util.Date;

/**
 * Default test builder offers builder pattern methods in order to configure a
 * test case with test actions, variables and properties.
 *
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class DefaultTestBuilder extends AbstractTestBuilder {

    /**
     * Default constructor.
     */
    public DefaultTestBuilder() {
        super();
    }

    /**
     * Constructor using Spring bean application context.
     * @param applicationContext
     */
    public DefaultTestBuilder(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public void name(String name) {
        getTestCase().setBeanName(name);
        getTestCase().setName(name);
    }

    @Override
    public void description(String description) {
        getTestCase().setDescription(description);
    }

    @Override
    public void author(String author) {
        getTestCase().getMetaInfo().setAuthor(author);
    }

    @Override
    public void packageName(String packageName) {
        getTestCase().setPackageName(packageName);
    }

    @Override
    public void status(TestCaseMetaInfo.Status status) {
        getTestCase().getMetaInfo().setStatus(status);
    }

    @Override
    public void creationDate(Date date) {
        getTestCase().getMetaInfo().setCreationDate(date);
    }

    @Override
    public void variable(String name, Object value) {
        getVariables().put(name, value);
    }

    @Override
    public CreateVariablesActionDefinition variables() {
        CreateVariablesActionDefinition definition = TestActions.createVariables();
        action(definition);
        return definition;
    }

    @Override
    public CreateVariablesAction setVariable(String variableName, String value) {
        CreateVariablesAction action = TestActions.createVariable(variableName, value);
        action(action);
        return action;
    }

    @Override
    public void applyBehavior(TestBehavior behavior) {
        behavior.setApplicationContext(getApplicationContext());
        behavior.apply(this);
    }

    @Override
    public void doFinally(TestAction... actions) {
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                getTestCase().getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                getTestCase().getFinallyChain().add(((AbstractActionDefinition<?>) action).getAction());
            } else if (!action.getClass().isAnonymousClass()) {
                getTestCase().getActions().remove(action);
                getTestCase().getFinallyChain().add(action);
            } else {
                getTestCase().getFinallyChain().add(action);
            }
        }
    }
}
