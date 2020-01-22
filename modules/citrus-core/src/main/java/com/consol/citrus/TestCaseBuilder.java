package com.consol.citrus;

import java.util.Date;

import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.FinallySequence;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Christoph Deppisch
 */
public interface TestCaseBuilder extends ApplicationContextAware {

    /**
     * Builds the test case.
     * @return
     */
    TestCase getTestCase();

    /**
     * Set test class.
     * @param type
     */
    void testClass(Class<?> type);

    /**
     * Set custom test case name.
     * @param name
     */
    void name(String name);

    /**
     * Adds description to the test case.
     *
     * @param description
     */
    void description(String description);

    /**
     * Adds author to the test case.
     *
     * @param author
     */
    void author(String author);

    /**
     * Sets custom package name for this test case.
     * @param packageName
     */
    void packageName(String packageName);

    /**
     * Sets test case status.
     *
     * @param status
     */
    void status(TestCaseMetaInfo.Status status);

    /**
     * Sets the creation date.
     *
     * @param date
     */
    void creationDate(Date date);

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case and return its value.
     *
     * @param name
     * @param value
     * @return
     */
    <T> T variable(String name, T value);

    /**
     * Prepare and add a custom container implementation.
     * @param container
     * @return
     */
    <T extends AbstractActionContainer, B extends AbstractTestContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container);

    /**
     * Prepare and add a custom container implementation.
     * @param builder
     * @return
     */
    <T extends TestActionContainerBuilder<? extends AbstractActionContainer, ?>> T container(T builder);

    /**
     * Adds sequence of test actions to finally block.
     * @return
     */
    FinallySequence.Builder doFinally();
}
