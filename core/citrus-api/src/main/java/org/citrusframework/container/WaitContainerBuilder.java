/*
 * Copyright the original author or authors.
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

import java.io.File;
import java.time.Duration;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.condition.Condition;

public interface WaitContainerBuilder<T extends TestAction, B extends TestActionBuilder<T>, C extends Condition>
        extends ActionBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Condition to wait for during execution.
     * @param condition The condition to add to the wait action
     * @return The wait action
     */
    WaitContainerBuilder<T, B, C> condition(C condition);

    /**
     * Sets custom condition builder.
     */
    ConditionBuilder<T, C, ?> condition(ConditionBuilder<T, C, ?> conditionBuilder);

    <CB extends MessageConditionBuilder<T, C, CB>> CB message();

    <CB extends ActionConditionBuilder<T, C, CB>> CB execution();

    <CB extends HttpConditionBuilder<T, C, CB>> CB http();

    <CB extends FileConditionBuilder<T, C, CB>> CB file();

    WaitContainerBuilder<T, B, C> interval(Long interval);

    WaitContainerBuilder<T, B, C> interval(String interval);

    WaitContainerBuilder<T, B, C> milliseconds(long milliseconds);

    WaitContainerBuilder<T, B, C> milliseconds(String milliseconds);

    WaitContainerBuilder<T, B, C> seconds(double seconds);

    WaitContainerBuilder<T, B, C> time(Duration duration);

    interface BuilderFactory {

        WaitContainerBuilder<?, ?, ?> waitFor();

    }

    interface ConditionBuilder<T extends TestAction, C extends Condition, B extends ConditionBuilder<T, C, B>>
            extends TestActionBuilder<T> {

        /**
         * The interval in milliseconds to use between each test of the condition
         * @param interval The interval to use
         * @return The altered WaitBuilder
         */
        B interval(Long interval);

        /**
         * The interval in milliseconds to use between each test of the condition
         * @param interval The interval to use
         * @return The altered WaitBuilder
         */
        B interval(String interval);

        B milliseconds(long milliseconds);

        B milliseconds(String milliseconds);

        B seconds(double seconds);

        B time(Duration duration);

        C getCondition();
    }

    interface MessageConditionBuilder<T extends TestAction, C extends Condition, B extends MessageConditionBuilder<T, C, B>>
            extends ConditionBuilder<T, C, B> {

        B name(String messageName);
    }

    interface FileConditionBuilder<T extends TestAction, C extends Condition, B extends FileConditionBuilder<T, C, B>>
            extends ConditionBuilder<T, C, B> {

        /**
         * Wait for given file path.
         */
        B path(String filePath);

        /**
         * Wait for given file resource.
         */
        B resource(File file);
    }

    interface HttpConditionBuilder<T extends TestAction, C extends Condition, B extends HttpConditionBuilder<T, C, B>>
            extends ConditionBuilder<T, C, B> {

        /**
         * Sets the Http endpoint URL.
         */
        B url(String requestUrl);

        /**
         * Sets the Http connection timeout.
         */
        B timeout(String timeout);

        /**
         * Sets the Http connection timeout.
         */
        B timeout(Long timeout);

        /**
         * Sets the Http status code to check.
         */
        B status(int status);

        /**
         * Sets the Http method.
         */
        B method(String method);
    }

    interface ActionConditionBuilder<T extends TestAction, C extends Condition, B extends ActionConditionBuilder<T, C, B>>
            extends ConditionBuilder<T, C, B> {

        /**
         * Sets the test action to execute and wait for.
         */
        B action(TestAction action);

        /**
         * Sets the test action to execute and wait for.
         */
        B action(TestActionBuilder<?> action);
    }
}
