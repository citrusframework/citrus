package com.consol.citrus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestReporters;

/**
 * This class represents a test suite instance.
 * The test suite is started with a given list of initializing tasks,
 * to be executed before test case execution. Similar to that the test suite is
 * followed by a given list of destroying tasks at the end.
 *
 * Usually these initializing/destroying tasks are
 * injected to the TestSuite via spring configuration.
 *
 * After the initializing tasks the test suite loads and runs a given set of test cases,
 * that are specified by their unique name. Therefore the test suite needs a
 * spring application context containing the test beans. The context usually
 * should be handed over in init method.
 *
 * Successful and failed test operations are counted in private members and
 * should be reported at the end of the test suite.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 *
 */
public class TestSuite implements BeanNameAware {
    /** Counter members for successful and failed operations */
    private int cntTests = 0;
    private int cntActions = 0;
    private int cntCasesSuccess = 0;
    private int cntCasesFail = 0;
    private int cntSkipped = 0;

    /** List of tasks before, between and after */
    private List tasksBefore = new ArrayList();
    private List tasksBetween = new ArrayList();
    private List tasksAfter = new ArrayList();

    /** List containing test name patterns to include or exclude from test suite run */
    private List includeTests;
    private List excludeTests;

    private String name = "";

    private int threadCount = 1;

    private Stack testPool = new Stack();
    private Stack testRunner = new Stack();

    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.0");
    
    @Autowired
    private TestContext context;
    
    @Autowired
    private TestReporters reporters;

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestSuite.class);

    /**
     * Excecute tasks before the start of test suite
     * @param ctx ApplicationContext containing test and service beans
     * @return success flag
     */
    public boolean beforeSuite() {
        reporters.onStart(this);

        log.info("Found " + tasksBefore.size() + " tasks in init phase");

        for(int i=0; i<tasksBefore.size();i++)  {
            final TestAction testAction = (TestAction)tasksBefore.get(i);

            try {
                /* Executing test action and validate its success */
                testAction.execute(context);
            } catch (Exception e) {
                log.error("Task failed "
                        + testAction.getClass().getName()
                        + "Nested exception is: ", e);

                reporters.onStartFailure(this, e);

                afterSuite();

                return false;
            }
        }

        reporters.onStartSuccess(this);

        return true;
    }

    /**
     * Execute tasks after test suite work, e.g. shutdown of db connection
     * @throws CitrusRuntimeException
     * @return boolean flag marking success
     */
    public boolean afterSuite() {
        boolean success = true;

        reporters.onFinish(this);

        if (log.isDebugEnabled()) {
            log.info("Found " + tasksAfter.size() + " tasks after");
        }

        for(int i=0; i<tasksAfter.size();i++)  {
            final TestAction testAction = (TestAction)tasksAfter.get(i);

            try {
                /* Executing test action and validate its success */
                testAction.execute(context);
            } catch (Exception e) {
                log.error("Task failed "
                        + testAction.getClass().getName()
                        + "Nested exception is: ", e);
                log.error("Continue finishing TestSuite");
                success = false;
            }
        }

        if (success) {
            reporters.onFinishSuccess(this);
            reporters.generateReport(this);
        } else {
            reporters.onFinishFailure(this, new CitrusRuntimeException("Error in clean up phase"));
            reporters.generateReport(this);
        }

        return success;
    }

    /**
     * Method running test cases
     * @param tests tests to be executed, specified by unique name
     * @return boolean flag marking success
     */
    public boolean run(TestCase[] tests) {
        cntTests = tests.length;

        for(int i=0; i<tests.length;i++)  {
            TestCase testCase = tests[i];

            if (testCase.getMetaInfo().getStatus().equals(Status.DRAFT) || testCase.getMetaInfo().getStatus().equals(Status.DISABLED)) {
                skipTest(testCase);
                continue;
            }

            /* check if current test is included in test suite run */
            if (includeTests != null && !isIncluded(testCase)) {
                skipTest(testCase);
                continue;
            }

            /* check if current test is excluded from test suite run */
            if (excludeTests != null && isExcluded(testCase)) {
                skipTest(testCase);
                continue;
            }

            testPool.push(testCase);
        }

        for (int j = 1; j <= threadCount; j++) {
            TestRunner runner = new TestRunner(this);
            testRunner.push(runner);
            runner.start();
        }

        while (!testRunner.isEmpty()) {
            try {
                ((TestRunner)testRunner.pop()).getThread().join();
            } catch (InterruptedException e) {
                log.warn("Error while joining runner thread", e);
            }
        }

        return (cntCasesFail == 0);
    }

    /**
     * Method running test cases
     * @param tests tests to be executed, specified by unique name
     * @return boolean flag marking success
     */
    public boolean run(TestCase testCase) {
        boolean success = true;

        reporters.onTestStart(testCase);

        try {
            /* Execute test case */
            testCase.execute();
            testCase.finish();

            ++cntCasesSuccess;
            reporters.onTestSuccess(testCase);
        } catch (Exception e) {
            success = false;
            ++cntCasesFail;
            reporters.onTestFailure(testCase, e);
        }

        cntActions += testCase.getCountActions();

        reporters.onTestFinish(testCase);

        log.info("FINISHED TEST 1/1 (100%)");
        log.info("");

        return success;
    }

    public void beforeTest() {
        if (tasksBetween == null || tasksBetween.isEmpty()) {
            return;
        }

        /* execute tasks between test cases */
        if (log.isDebugEnabled()) {
            log.debug("Found " + tasksBetween.size() + " tasks between tests");
        }

        for(int j=0; j<tasksBetween.size();j++)  {
            final TestAction testAction = (TestAction)tasksBetween.get(j);

            /* Executing test action */
            testAction.execute(context);
        }
    }

    /**
     * Method to check wheather a test is excluded from test suite run
     * @param testName name of current test case
     * @return boolean flag to mark possible exclusion
     */
    private boolean isExcluded(TestCase test) {
        Iterator it;
        it = excludeTests.iterator();

        String testName = test.getName();

        /* check if current test case name matches an excluding name pattern */
        while (it.hasNext()) {
            String namePattern = (String) it.next();

            if (namePattern.startsWith(CitrusConstants.SEARCH_WILDCARD) && namePattern.endsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(CitrusConstants.SEARCH_WILDCARD.length(), namePattern.length() - CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.indexOf(namePattern) != -1) {
                    return true;
                }
            } else if (namePattern.startsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.endsWith(namePattern)) {
                    return true;
                }
            } else if (namePattern.endsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(0, namePattern.length() - CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.startsWith(namePattern)) {
                    return true;
                }
            } else {
                if (excludeTests.contains(testName))
                    return true;
            }
        }

        return false;
    }

    /**
     * Method to check wheather a test case is explicitly included in test suite run
     * @param testName test case name
     * @return boolean flag to mark possible inclusion
     */
    private boolean isIncluded(TestCase test) {
        Iterator it;
        it = includeTests.iterator();

        String testName = test.getName();

        /* check if current test case name matches an including name pattern */
        while (it.hasNext()) {
            String namePattern = (String) it.next();

            if (namePattern.startsWith(CitrusConstants.SEARCH_WILDCARD) && namePattern.endsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(CitrusConstants.SEARCH_WILDCARD.length(), namePattern.length() - CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.indexOf(namePattern) != -1) {
                    return true;
                }
            } else if (namePattern.startsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.endsWith(namePattern)) {
                    return true;
                }
            } else if (namePattern.endsWith(CitrusConstants.SEARCH_WILDCARD)) {
                namePattern = namePattern.substring(0, namePattern.length() - CitrusConstants.SEARCH_WILDCARD.length());
                if (testName.startsWith(namePattern)) {
                    return true;
                }
            } else {
                if (includeTests.contains(testName))
                    return true;
            }
        }

        return false;
    }

    /**
     * Spring property setter.
     * Injects the tasks after
     * @param tasksAfter
     */
    public void setTasksAfter(List tasksAfter) {
        this.tasksAfter = tasksAfter;
    }

    /**
     * Spring property setter.
     * Injects the tasks before
     * @param tasksBefore
     */
    public void setTasksBefore(List tasksBefore) {
        this.tasksBefore = tasksBefore;
    }

    /**
     * Spring property setter.
     * Injects the tasks between
     * @param tasksBetween
     */
    public void setTasksBetween(List tasksBetween) {
        this.tasksBetween = tasksBetween;
    }

    /**
     * Spring property setter.
     * Injects the excluding name patterns
     * @param excludeTests
     */
    public void setExcludeTests(List excludeTests) {
        this.excludeTests = excludeTests;
    }

    /**
     * Spring property setter.
     * Injects the including name patterns
     * @param includeTests
     */
    public void setIncludeTests(List includeTests) {
        this.includeTests = includeTests;
    }

    public boolean hasFailedTests() {
        return cntCasesFail > 0;
    }

    public void setBeanName(String beanName) {
        this.name = beanName;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the cntCasesSuccess
     */
    public int getCntCasesSuccess() {
        return cntCasesSuccess;
    }

    /**
     * @return the cntCasesFail
     */
    public int getCntCasesFail() {
        return cntCasesFail;
    }

    public int getCntTests() {
        return cntTests;
    }

    public int getCntSkipped() {
        return cntSkipped;
    }

    public int getCntActions() {
        return cntActions;
    }

    /**
     * @param reporter the reporter to set
     */
    public void setReporters(TestReporters reporters) {
        this.reporters = reporters;
    }

    /**
     * Try to get next test from stack
     * @return
     */
    public TestCase nextTest() throws EmptyStackException {
        synchronized (testPool) {
            return (TestCase)testPool.pop();
        }
    }

    /**
     * Report test succeess
     * @param testCase
     */
    public synchronized void succeedTest(TestCase testCase) {
        ++cntCasesSuccess;
        reporters.onTestSuccess(testCase);
    }

    /**
     * Report test failure
     * @param testCase
     * @param e
     */
    public synchronized void failTest(TestCase testCase, Exception e) {
        ++cntCasesFail;
        reporters.onTestFailure(testCase, e);
    }

    /**
     * Report test skip
     * @param testCase
     */
    public synchronized void skipTest(TestCase testCase) {
        cntSkipped++;
        reporters.onTestSkipped(testCase);
    }

    /**
     * Report test finish
     * @param testCase
     */
    public synchronized void finishTest(TestCase testCase) {
        cntActions += testCase.getCountActions();

        reporters.onTestFinish(testCase);

        log.info("FINISHED TEST " + (cntCasesFail+cntCasesSuccess+cntSkipped) + "/" + cntTests + " (" + decFormat.format(((double)(cntCasesFail+cntCasesSuccess+cntSkipped) / cntTests)*100) + "%)");
        log.info("");
    }

    /**
     * Report test start
     * @param testCase
     */
    public void startTest(TestCase testCase) {
        reporters.onTestStart(testCase);
    }

    /**
     * @param threadCount the threadCount to set
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
