package org.citrusframework;

import org.citrusframework.container.AbstractActionContainer;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractExceptionContainerBuilder<T extends AbstractActionContainer, S extends AbstractExceptionContainerBuilder<T, S>> extends AbstractTestContainerBuilder<T, S> {

    /**
     * Fills container with actions.
     * @param actions
     * @return
     */
    public S when(TestAction... actions) {
        return actions(actions);
    }

    /**
     * Fills container with actions.
     * @param actions
     * @return
     */
    public S when(TestActionBuilder<?>... actions) {
        return actions(actions);
    }
}
