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

package org.citrusframework.api.actions.camel;

import java.util.function.BiConsumer;

import org.citrusframework.TestAction;
import org.citrusframework.context.TestContext;

public interface CamelVerifyRouteStatsActionBuilder<T extends TestAction, B extends CamelVerifyRouteStatsActionBuilder<T, B>>
        extends CamelRouteActionBuilderBase<T, B> {

    B completed(long completed);

    B failed(long failed);

    B stats(String expectedStatsJson);

    /**
     * Supplies a custom validation callback that receives the deserialized route statistics and
     * the current {@link TestContext}. The callback is invoked after the raw JSON has been
     * obtained from the managed route MBean, allowing arbitrary, type-safe assertions against the
     * route stats model object.
     *
     * @param validator BiConsumer accepting the deserialized route stats and the test context
     * @param <R>       the route stats model type (e.g. {@code CamelRouteStats})
     * @return this builder
     */
    <R> B validate(BiConsumer<R, TestContext> validator);
}
