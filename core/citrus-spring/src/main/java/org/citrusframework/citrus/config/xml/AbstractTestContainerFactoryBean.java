package org.citrusframework.citrus.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.citrus.AbstractTestContainerBuilder;
import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.container.TestActionContainer;

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
