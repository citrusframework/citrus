/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.dsl.builder;

import com.consol.citrus.condition.*;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.Wait;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

import java.io.File;
import java.util.Stack;

/**
 * Wait action pauses test execution until a condition is satisfied. If the condition is not satisfied after the
 * configured timeout then the test exits with an error.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class WaitBuilder extends AbstractTestContainerBuilder<Wait> {

    private final Stack<AbstractActionContainer> containers;

    /**
     * Constructor using designer and action field.
     * @param designer
     * @param action
     */
    public WaitBuilder(TestDesigner designer, Wait action, Stack<AbstractActionContainer> containers) {
        super(designer, action);
        this.containers = containers;
    }

    /**
     * Constructor using runner and action field.
     * @param runner
     * @param action
     */
    public WaitBuilder(TestRunner runner, Wait action) {
        super(runner, action);
        this.containers = new Stack<>();
    }

    /**
     * Condition to wait for during execution.
     * @param condition
     * @return
     */
    public WaitConditionBuilder condition(Condition condition) {
        container.setCondition(condition);

        if (designer != null) {
            designer.action(this);
        } else if (runner != null) {
            runner.run(this.build());
        }

        return new WaitConditionBuilder<>(action, condition);
    }

    /**
     * The HTTP condition to wait for during execution.
     * @param url
     * @return
     */
    public WaitHttpConditionBuilder http(String url) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(url);

        container.setCondition(condition);
        if (designer != null) {
            designer.action(this);
        } else if (runner != null) {
            runner.run(this.build());
        }

        return new WaitHttpConditionBuilder(container, condition);
    }

    /**
     * The message condition to wait for during execution.
     * @param name
     * @return
     */
    public WaitConditionBuilder message(String name) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(name);
        return condition(condition);
    }

    /**
     * The test action condition to wait for during execution.
     * @return
     */
    public WaitActionConditionBuilder execution() {
        ActionCondition condition = new ActionCondition();
        container.setCondition(condition);
        containers.push(this.build());
        return new WaitActionConditionBuilder(container, condition, this);
    }

    /**
     * The file condition to wait for during execution.
     * @param path
     * @return
     */
    public WaitConditionBuilder file(String path) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(path);
        return condition(condition);
    }

    /**
     * The file condition to wait for during execution.
     * @param file
     * @return
     */
    public WaitConditionBuilder file(File file) {
        FileCondition condition = new FileCondition();
        condition.setFile(file);
        return condition(condition);
    }
}
