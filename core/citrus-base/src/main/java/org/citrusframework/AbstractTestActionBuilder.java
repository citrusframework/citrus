package org.citrusframework;

/**
 * @author Christoph Deppisch
 */
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
