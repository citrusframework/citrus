package org.citrusframework;

/**
 * Provides methods to create instances of {@link TestResult} for different outcomes of a test case.
 * Implementors may decide to create TestResults with specific parameters derived from the {@link TestCase}.
 */
public interface TestResultInstanceProvider {

    TestResult createSuccess(TestCase testCase);
    TestResult createFailed(TestCase testCase, Throwable throwable);
    TestResult createSkipped(TestCase testCase);

}
