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

import java.time.Duration;

import org.citrusframework.api.container.IteratingActionContainer;
import org.citrusframework.api.container.IteratingConditionExpression;
import org.citrusframework.api.container.IteratingContainerBuilder;

public abstract class AbstractIteratingContainerBuilder<T extends IteratingActionContainer, S extends AbstractIteratingContainerBuilder<T, S>>
        extends AbstractTestContainerBuilder<T, S> implements IteratingContainerBuilder<T, S> {

    protected String condition;
    protected IteratingConditionExpression conditionExpression;
    protected String indexName = "i";
    protected Duration timeout;
    protected int index;
    protected int start = 1;

    @Override
    public S condition(String condition) {
        this.condition = condition;
        return self;
    }

    @Override
    public S condition(IteratingConditionExpression condition) {
        this.conditionExpression = condition;
        return self;
    }

    @Override
    public S index(String name) {
        this.indexName = name;
        return self;
    }

    @Override
    public S timeout(Duration timeout) {
        this.timeout = timeout;
        return self;
    }

    @Override
    public S startsWith(int index) {
        this.start = index;
        return self;
    }

    /**
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @return the condition expression
     */
    public IteratingConditionExpression getConditionExpression() {
        return conditionExpression;
    }

    /**
     * @return the index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @return the timeout duration
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the start index
     */
    public int getStart() {
        return start;
    }

    @Override
    public T build() {
        if (condition == null && conditionExpression == null) {
            conditionExpression = (index, context) -> index > 10;
        }

        return super.build();
    }
}
