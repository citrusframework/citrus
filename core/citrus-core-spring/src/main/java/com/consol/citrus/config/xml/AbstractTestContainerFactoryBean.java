package com.consol.citrus.config.xml;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.AbstractTestContainerBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.container.TestActionContainer;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractTestContainerFactoryBean<T extends TestActionContainer, B extends AbstractTestContainerBuilder<?, ?>> extends AbstractTestActionFactoryBean<T, B> {

    private List<TestAction> actions = new ArrayList<>();

    /**
     * Sets the test actions.
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * Adds test actions to container when building object.
     * @param container
     * @return
     * @throws Exception
     */
    public T getObject(T container) throws Exception {
        container.setActions(actions);
        return container;
    }
}
