package org.citrusframework;

import org.citrusframework.container.AbstractIteratingActionContainer;
import org.citrusframework.container.IteratingConditionExpression;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractIteratingContainerBuilder<T extends AbstractIteratingActionContainer, S extends AbstractIteratingContainerBuilder<T, S>> extends AbstractTestContainerBuilder<T, S> {

    protected String condition;
    protected IteratingConditionExpression conditionExpression;
    protected String indexName = "i";
    protected int index;
    protected int start = 1;

    /**
     * Adds a condition to this iterate container.
     * @param condition
     * @return
     */
    public S condition(String condition) {
        this.condition = condition;
        return self;
    }

    /**
     * Adds a condition expression to this iterate container.
     * @param condition
     * @return
     */
    public S condition(IteratingConditionExpression condition) {
        this.conditionExpression = condition;
        return self;
    }

    /**
     * Sets the index variable name.
     * @param name
     * @return
     */
    public S index(String name) {
        this.indexName = name;
        return self;
    }

    /**
     * Sets the index start value.
     * @param index
     * @return
     */
    public S startsWith(int index) {
        this.start = index;
        return self;
    }

    @Override
    public T build() {
        if (condition == null && conditionExpression == null) {
            conditionExpression = (index, context) -> index > 10;
        }

        return super.build();
    }

    /**
     * Gets the condition.
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the condition.
     * @return the conditionExpression
     */
    public IteratingConditionExpression getConditionExpression() {
        return conditionExpression;
    }

    /**
     * Gets the indexName.
     * @return the indexName
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Gets the index.
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the start index.
     * @return
     */
    public int getStart() {
        return start;
    }
}
