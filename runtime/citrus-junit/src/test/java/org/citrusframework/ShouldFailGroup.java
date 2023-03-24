package org.citrusframework;

/**
 * Test group to mark a test should fail on purpose. Group is executed during Maven integration-test phase in a separate
 * execution in order to separate those tests form normal success test cases.
 *
 * This group is used in both JUnit and TestNG test suites where JUnit uses the class itself as a {@link org.junit.experimental.categories.Category} and
 * TestNG uses the class name as test group name in {@link org.testng.annotations.Test}.
 *
 * @author Christoph Deppisch
 */
public final class ShouldFailGroup {

    /**
     * Prevent instantiation
     */
    private ShouldFailGroup() {
        super();
    }
}
