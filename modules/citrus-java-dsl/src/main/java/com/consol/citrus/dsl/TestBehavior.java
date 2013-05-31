package com.consol.citrus.dsl;

import com.consol.citrus.TestAction;

import java.util.List;
import java.util.Map;

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
    void apply();

    /**
     * Provides access to this builders test action sequence.
     * @return
     */
    List<TestAction> getTestActions();

    /**
     * Provides access to this builders finally test actions.
     * @return
     */
    List<TestAction> getFinallyActions();

    /**
     * Provides access to this builders test variable definitions.
     * @return
     */
    Map<String, Object> getVariableDefinitions();
}
