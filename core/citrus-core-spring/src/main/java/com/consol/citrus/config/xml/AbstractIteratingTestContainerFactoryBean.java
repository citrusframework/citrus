package com.consol.citrus.config.xml;

import com.consol.citrus.AbstractIteratingContainerBuilder;
import com.consol.citrus.container.AbstractIteratingActionContainer;
import com.consol.citrus.container.IteratingConditionExpression;

/**
 * @author Christoph Deppisch
 */
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

}
