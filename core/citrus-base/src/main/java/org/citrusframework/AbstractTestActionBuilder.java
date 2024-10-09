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

public abstract class AbstractTestActionBuilder<T extends TestAction, S extends TestActionBuilder<T>> implements TestActionBuilder<T> {

    protected final S self;

    private String name;
    private String description;
    private TestActor actor;

    protected AbstractTestActionBuilder() {
        self = (S) this;
    }

    /**
     * Sets the test action name.
     * @param name the test action name.
     * @return
     */
    public S name(String name) {
        this.name = name;
        return self;
    }

    /**
     * Sets the description.
     * @param description
     * @return
     */
    public S description(String description) {
        this.description = description;
        return self;
    }

    /**
     * Sets the test actor for this action.
     * @param actor the actor.
     * @return
     */
    public S actor(TestActor actor) {
        this.actor = actor;
        return self;
    }

    /**
     * Obtains the name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Obtains the description.
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtains the actor.
     * @return
     */
    public TestActor getActor() {
        return actor;
    }
}
