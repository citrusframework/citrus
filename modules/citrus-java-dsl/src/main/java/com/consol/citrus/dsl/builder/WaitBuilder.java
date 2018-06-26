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
    public Wait condition(Condition condition) {
        container.setCondition(condition);
        return this.buildAndRun();
    }

    /**
     * The HTTP condition to wait for during execution.
     * @return
     */
    public WaitHttpConditionBuilder http() {
        HttpCondition condition = new HttpCondition();
        container.setCondition(condition);
        return new WaitHttpConditionBuilder(condition, this);
    }

    /**
     * The message condition to wait for during execution.
     * @return
     */
    public WaitMessageConditionBuilder message() {
        MessageCondition condition = new MessageCondition();
        container.setCondition(condition);
        return new WaitMessageConditionBuilder(condition, this);
    }

    /**
     * The test action condition to wait for during execution.
     * @return
     */
    public WaitActionConditionBuilder execution() {
        ActionCondition condition = new ActionCondition();
        container.setCondition(condition);
        containers.push(container);
        return new WaitActionConditionBuilder(container, condition, this);
    }

    /**
     * The file condition to wait for during execution.
     * @return
     */
    public WaitFileConditionBuilder file() {
        FileCondition condition = new FileCondition();
        container.setCondition(condition);
        return new WaitFileConditionBuilder(condition, this);
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public WaitBuilder seconds(String seconds) {
        container.setSeconds(seconds);
        return this;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public WaitBuilder seconds(Long seconds) {
        container.setSeconds(seconds.toString());
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitBuilder ms(String milliseconds) {
        container.setMilliseconds(milliseconds);
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitBuilder ms(Long milliseconds) {
        container.setMilliseconds(String.valueOf(milliseconds));
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitBuilder milliseconds(String milliseconds) {
        container.setMilliseconds(milliseconds);
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitBuilder milliseconds(Long milliseconds) {
        container.setMilliseconds(String.valueOf(milliseconds));
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public WaitBuilder interval(String interval) {
        container.setInterval(interval);
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public WaitBuilder interval(Long interval) {
        container.setInterval(String.valueOf(interval));
        return this;
    }

    /**
     * Finishes action build process.
     * @return
     */
    public Wait buildAndRun() {
        if (designer != null) {
            designer.action(this);
        } else if (runner != null) {
            runner.run(super.build());
        }

        return super.build();
    }
}
