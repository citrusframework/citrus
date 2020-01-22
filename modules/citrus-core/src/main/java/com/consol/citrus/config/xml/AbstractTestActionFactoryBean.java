package com.consol.citrus.config.xml;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActor;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Christoph Deppisch
 */
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
