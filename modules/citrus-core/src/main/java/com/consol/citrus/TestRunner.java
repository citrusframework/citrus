package com.consol.citrus;

import java.util.EmptyStackException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunner implements Runnable {
    private Thread t;

    private boolean running = false;

    private TestSuite testSuite;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    public TestRunner(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public void start() {
        log.info("About to start runner " + this);
        t = new Thread(this);
        t.start();
    }

    public void run() {
        running = true;
        log.debug("Runner starting ...");

        try {
            while (running) {
                TestCase testCase = testSuite.nextTest();

                if(log.isDebugEnabled()) {
                    log.debug("Runner executing test " + testCase.getName());
                }

                testSuite.startTest(testCase);

                try {
                    /* Execute test case */
                    testSuite.beforeTest();

                    testCase.execute();
                    testCase.finish();

                    testSuite.succeedTest(testCase);
                } catch (Exception e) {
                    testSuite.failTest(testCase, e);
                }

                testSuite.finishTest(testCase);
            }
        } catch (EmptyStackException e) {
            log.info("No more tests to execute - runner finished work");
        }

        log.debug("Runner finished");
    }

    public void stop() {
        log.info("About to stop runner " + this);
        running = false;
        t = null;
    }

    public Thread getThread() {
        return t;
    }
}
