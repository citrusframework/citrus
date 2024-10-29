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

import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.container.AbstractIteratingActionContainer;
import org.citrusframework.container.IteratingConditionExpression;

import java.time.Duration;

public abstract class AbstractIteratingTestContainerFactoryBean<T extends AbstractIteratingActionContainer, B extends AbstractIteratingContainerBuilder<?, ?>> extends AbstractTestContainerFactoryBean<T, B> {

    /**
     * Aborting condition.
     * @param condition
     */
    public void setCondition(String condition) {
        getBuilder().condition(condition);
    }

    /**
     * Aborting condition expression.
     * @param conditionExpression
     */
    public void setConditionExpression(IteratingConditionExpression conditionExpression) {
        getBuilder().condition(conditionExpression);
    }

    /**
     * Name of index variable.
     * @param indexName
     */
    public void setIndexName(String indexName) {
        getBuilder().index(indexName);
    }

    /**
     * Setter for index start.
     * @param start the start index value.
     */
    public void setStart(int start) {
        getBuilder().startsWith(start);
    }

    /**
     * Setter fot timeout duration.
     * @param timeout the maximum duration this action will take before ending in a timeout
     */
    public void setTimeout(Duration timeout) {
        getBuilder().timeout(timeout);
    }
}
