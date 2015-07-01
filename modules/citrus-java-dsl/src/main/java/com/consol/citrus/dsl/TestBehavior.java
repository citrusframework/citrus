package com.consol.citrus.dsl;

/**
 * Test apply interface applies to test builder classes adding all builder
 * methods to a test builder instance.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public interface TestBehavior extends TestBuilder {

    /**
     * Behavior building method.
     */
    void apply(TestBuilder target);
}
