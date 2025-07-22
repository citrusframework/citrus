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

package org.citrusframework.actions;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface SleepActionBuilder<T extends TestAction>
        extends ActionBuilder<T, SleepActionBuilder<T>>, TestActionBuilder<T> {

    SleepActionBuilder<T> milliseconds(Integer milliseconds);

    SleepActionBuilder<T> milliseconds(Long ms);

    SleepActionBuilder<T> milliseconds(String expression);

    SleepActionBuilder<T> seconds(Double seconds);

    SleepActionBuilder<T> seconds(Integer seconds);

    SleepActionBuilder<T> seconds(Long seconds);

    SleepActionBuilder<T> time(Duration duration);

    SleepActionBuilder<T> time(String expression, TimeUnit timeUnit);

    interface BuilderFactory {

        SleepActionBuilder<?> sleep();

        default SleepActionBuilder<?> delay() {
            return sleep();
        }

    }

}
