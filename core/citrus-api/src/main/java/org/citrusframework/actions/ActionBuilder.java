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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;

public interface ActionBuilder<T extends TestAction, B extends TestActionBuilder<T>> {

    /**
     * Sets the test action name.
     * @param name the test action name.
     * @return
     */
    B name(String name);

    /**
     * Sets the description.
     * @param description
     * @return
     */
    B description(String description);

    /**
     * Sets the test actor for this action.
     * @param actor the actor.
     * @return
     */
    B actor(TestActor actor);
}
