package org.citrusframework;

import java.util.Date;

/**
 * @author Christoph Deppisch
 */
public interface TestCaseBuilder {

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
     * Sets the test group names for this test.
     */
    void groups(String[] groups);

    /**
     * Adds a new variable definition to the set of test variables
     * for this test case and return its value.
     *
     * @param name
     * @param value
     * @return
     */
    <T> T variable(String name, T value);
}
