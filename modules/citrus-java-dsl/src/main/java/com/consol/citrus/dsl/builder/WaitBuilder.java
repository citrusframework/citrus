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

import com.consol.citrus.condition.ActionCondition;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.condition.HttpCondition;
import com.consol.citrus.condition.MessageCondition;
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
 * @since 2.4
 */
public class WaitBuilder extends AbstractTestContainerBuilder<Wait> {

    private final Stack<AbstractActionContainer> containers;

    /**
     * Constructor using designer and action field.
     *
     * @param designer The designer to execute this action in
     * @param action The container to add this action to
     */
    public WaitBuilder(TestDesigner designer, Wait action, Stack<AbstractActionContainer> containers) {
        super(designer, action);
        this.containers = containers;
    }

    /**
     * Constructor using runner and action field.
     *
     * @param runner The test runner to execute this action in
     * @param action The container to add this action to
     */
    public WaitBuilder(TestRunner runner, Wait action) {
        super(runner, action);
        this.containers = new Stack<>();
    }

    /**
     * Condition to wait for during execution.
     *
     * @param condition The condition to add to the wait action
     * @return The wait action
     */
    public Wait condition(Condition condition) {
        container.setCondition(condition);
        return this.buildAndRun();
    }

    /**
     * The HTTP condition to wait for during execution.
     *
     * @deprecated in favor of {@link #http()}
     */
    @Deprecated
    public WaitHttpConditionBuilder http(String url) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(url);
        container.setCondition(condition);
        this.buildAndRun();
        return new WaitHttpConditionBuilder(condition, this);
    }

    /**
     * The HTTP condition to wait for during execution.
     *
     * @return A WaitHttpConditionBuilder for further configuration
     */
    public WaitHttpConditionBuilder http() {
        HttpCondition condition = new HttpCondition();
        container.setCondition(condition);
        return new WaitHttpConditionBuilder(condition, this);
    }

    /**
     * The message condition to wait for during execution.
     *
     * @param name the message to wait on
     * @return WaitConditionBuilder for further configuration
     * @deprecated in favor of {@link #message()}
     */
    @Deprecated
    public WaitConditionBuilder message(String name) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(name);
        container.setCondition(condition);
        this.buildAndRun();
        return new WaitMessageConditionBuilder(condition, this);
    }

    /**
     * The message condition to wait for during execution.
     *
     * @return A WaitMessageConditionBuilder for further configuration
     */
    public WaitMessageConditionBuilder message() {
        MessageCondition condition = new MessageCondition();
        container.setCondition(condition);
        return new WaitMessageConditionBuilder(condition, this);
    }

    /**
     * The test action condition to wait for during execution.
     *
     * @return A WaitActionConditionBuilder for further configuration
     */
    public WaitActionConditionBuilder execution() {
        ActionCondition condition = new ActionCondition();
        container.setCondition(condition);
        containers.push(container);
        return new WaitActionConditionBuilder(container, condition, this);
    }

    /**
     * The file condition to wait for during execution.
     *
     * @deprecated in favor of {@link #file()}
     */
    @Deprecated
    public WaitFileConditionBuilder file(String filePath) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(filePath);
        container.setCondition(condition);
        this.buildAndRun();
        return new WaitFileConditionBuilder(condition, this);
    }

    /**
     * The file condition to wait for during execution.
     *
     * @deprecated in favor of {@link #file()}
     */
    @Deprecated
    public WaitFileConditionBuilder file(File file) {
        FileCondition condition = new FileCondition();
        condition.setFile(file);
        container.setCondition(condition);
        this.buildAndRun();
        return new WaitFileConditionBuilder(condition, this);
    }

    /**
     * The file condition to wait for during execution.
     *
     * @return A WaitFileConditionBuilder for further configuration
     */
    public WaitFileConditionBuilder file() {
        FileCondition condition = new FileCondition();
        container.setCondition(condition);
        return new WaitFileConditionBuilder(condition, this);
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     *
     * @param seconds The seconds to wait
     * @return The altered WaitBuilder
     */
    public WaitBuilder seconds(String seconds) {
        container.setSeconds(seconds);
        return this;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     *
     * @param seconds The seconds to wait
     * @return The altered WaitBuilder
     */
    public WaitBuilder seconds(Long seconds) {
        container.setSeconds(seconds.toString());
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     *
     * @param milliseconds The milliseconds to wait
     * @return The altered WaitBuilder
     * @deprecated in favor of {@link #milliseconds(String)}
     */
    @Deprecated
    public WaitBuilder ms(String milliseconds) {
        return milliseconds(milliseconds);
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     *
     * @param milliseconds The milliseconds to wait
     * @return The altered WaitBuilder
     * @deprecated in favor of {@link #milliseconds(Long)}
     */
    @Deprecated
    public WaitBuilder ms(Long milliseconds) {
        return milliseconds(milliseconds);
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     *
     * @param milliseconds The milliseconds to wait
     * @return The altered WaitBuilder
     */
    public WaitBuilder milliseconds(String milliseconds) {
        container.setMilliseconds(milliseconds);
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     *
     * @param milliseconds The milliseconds to wait
     * @return The altered WaitBuilder
     */
    public WaitBuilder milliseconds(Long milliseconds) {
        container.setMilliseconds(String.valueOf(milliseconds));
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     *
     * @param interval  The interval to use
     * @return The altered WaitBuilder
     */
    public WaitBuilder interval(String interval) {
        container.setInterval(interval);
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     *
     * @param interval The interval to use
     * @return The altered WaitBuilder
     */
    public WaitBuilder interval(Long interval) {
        container.setInterval(String.valueOf(interval));
        return this;
    }

    /**
     * Finishes action build process.
     *
     * @return The Wait action to execute
     */
    public Wait buildAndRun() {
        if (designer != null) {
            designer.action(this);
        } else if (runner != null) {
            runner.run(super.build());
        }

        return super.build();
    }

    Stack<AbstractActionContainer> getContainers() {
        return containers;
    }
}
