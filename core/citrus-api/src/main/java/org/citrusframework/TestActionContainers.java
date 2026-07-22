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

package org.citrusframework;

import org.citrusframework.api.container.ApplyTemplateBuilder;
import org.citrusframework.api.container.AssertContainerBuilder;
import org.citrusframework.api.container.AsyncContainerBuilder;
import org.citrusframework.api.container.CatchContainerBuilder;
import org.citrusframework.api.container.ConditionalContainerBuilder;
import org.citrusframework.api.container.FinallyContainerBuilder;
import org.citrusframework.api.container.IterateContainerBuilder;
import org.citrusframework.api.container.ParallelContainerBuilder;
import org.citrusframework.api.container.RepeatOnErrorUntilTrueContainerBuilder;
import org.citrusframework.api.container.RepeatUntilTrueContainerBuilder;
import org.citrusframework.api.container.SequentialContainerBuilder;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.api.container.TimerContainerBuilder;
import org.citrusframework.api.container.WaitContainerBuilder;

/**
 * Interface combines domain specific language methods for all test action containers available in Citrus.
 */
public interface TestActionContainers extends
        ApplyTemplateBuilder.BuilderFactory,
        AssertContainerBuilder.BuilderFactory,
        AsyncContainerBuilder.BuilderFactory,
        CatchContainerBuilder.BuilderFactory,
        ConditionalContainerBuilder.BuilderFactory,
        FinallyContainerBuilder.BuilderFactory,
        IterateContainerBuilder.BuilderFactory,
        ParallelContainerBuilder.BuilderFactory,
        RepeatOnErrorUntilTrueContainerBuilder.BuilderFactory,
        RepeatUntilTrueContainerBuilder.BuilderFactory,
        SequentialContainerBuilder.BuilderFactory,
        TimerContainerBuilder.BuilderFactory,
        WaitContainerBuilder.BuilderFactory {

    /**
     * Generic Java DSL container builder.
     */
    <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container);
}
