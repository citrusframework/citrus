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

package org.citrusframework.config.xml;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActor;
import org.springframework.beans.factory.FactoryBean;

public abstract class AbstractTestActionFactoryBean<T extends TestAction, B extends AbstractTestActionBuilder<?, ?>> implements FactoryBean<T> {

    /**
     * Set the bean name for this test action.
     * @param name the test action name.
     */
    public void setName(String name) {
        getBuilder().name(name);
    }

    /**
     * Sets the test action description.
     * @param description the description to set.
     */
    public void setDescription(String description) {
        getBuilder().description(description);
    }

    /**
     * Sets the test action actor.
     * @param actor the actor to set.
     */
    public void setActor(TestActor actor) {
        getBuilder().actor(actor);
    }

    /**
     * Provides the test action builder implementation.
     * @return the test action builder for this particular factory bean.
     */
    protected abstract B getBuilder();
}
