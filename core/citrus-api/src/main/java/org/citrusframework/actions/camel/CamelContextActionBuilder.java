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

package org.citrusframework.actions.camel;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface CamelContextActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    <S extends CamelCreateContextActionBuilder<T, S>> S create();

    <S extends CamelCreateContextActionBuilder<T, S>> S create(String contextName);

    <S extends CamelStartContextActionBuilder<T, S>> S start();

    <S extends CamelStartContextActionBuilder<T, S>> S start(String contextName);

    <S extends CamelStopContextActionBuilder<T, S>> S stop();

    <S extends CamelStopContextActionBuilder<T, S>> S stop(String contextName);
}
