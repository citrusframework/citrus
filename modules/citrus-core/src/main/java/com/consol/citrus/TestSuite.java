/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;

/**
 * This class represents a test suite instance.
 * The test suite is started with a given list of initializing tasks,
 * to be executed before. Similar to that the test suite is
 * followed by a list of tasks at the end.
 *
 * Usually these initializing/destroying tasks are
 * injected via spring IoC container.
 *
 * After initialization the test suite loads and runs a given set of test cases via Spring
 * application context. The context usually is handed over in init method.
 *
 * Successful and failed test operations are counted in private members and
 * should be reported at the end of the test suite.
 *
 * @author Christoph Deppisch
 * @since 2006
 *
 */
public class TestSuite implements BeanNameAware {
    /** List of tasks before, between and after */
    private List<TestAction> tasksBefore = new ArrayList<TestAction>();
    private List<TestAction> tasksBetween = new ArrayList<TestAction>();
    private List<TestAction> tasksAfter = new ArrayList<TestAction>();

    /** Test suite name */
    private String name = "";

    @Autowired
    private TestContext context = new TestContext();
    
    @Autowired
    private TestSuiteListeners testSuiteListener = new TestSuiteListeners();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TestSuite.class);

    /**
     * Execute tasks before the start of test suite
     * @param ctx ApplicationContext containing test and service beans
     * @return success flag
     */
    public boolean beforeSuite() {
        testSuiteListener.onStart(this);

        log.info("Found " + tasksBefore.size() + " tasks in init phase");

        for(int i=0; i<tasksBefore.size();i++)  {
            final TestAction testAction = tasksBefore.get(i);

            try {
                /* Executing test action and validate its success */
                testAction.execute(context);
            } catch (Exception e) {
                log.error("Task failed "
                        + testAction.getClass().getName()
                        + "Nested exception is: ", e);

                testSuiteListener.onStartFailure(this, e);

                afterSuite();

                return false;
            }
        }

        testSuiteListener.onStartSuccess(this);

        return true;
    }

    /**
     * Execute tasks after test suite
     * @throws CitrusRuntimeException
     * @return boolean flag marking success
     */
    public boolean afterSuite() {
        boolean success = true;

        testSuiteListener.onFinish(this);

        if (log.isDebugEnabled()) {
            log.info("Found " + tasksAfter.size() + " tasks after");
        }

        for(int i=0; i<tasksAfter.size();i++)  {
            final TestAction testAction = tasksAfter.get(i);

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
            testSuiteListener.onFinishSuccess(this);
        } else {
            testSuiteListener.onFinishFailure(this, new CitrusRuntimeException("Error in clean up phase"));
        }

        return success;
    }

    /**
     * Method running tasks before a test case.
     */
    public void beforeTest() {
        if (tasksBetween == null || tasksBetween.isEmpty()) {
            return;
        }

        /* execute tasks between test cases */
        if (log.isDebugEnabled()) {
            log.debug("Found " + tasksBetween.size() + " tasks between tests");
        }

        for(int j=0; j<tasksBetween.size();j++)  {
            final TestAction testAction = tasksBetween.get(j);

            /* Executing test action */
            testAction.execute(context);
        }
    }

    /**
     * Injected tasks after.
     * @param tasksAfter
     */
    public void setTasksAfter(List<TestAction> tasksAfter) {
        this.tasksAfter = tasksAfter;
    }

    /**
     * Injected tasks before
     * @param tasksBefore
     */
    public void setTasksBefore(List<TestAction> tasksBefore) {
        this.tasksBefore = tasksBefore;
    }

    /**
     * Injected tasks before test.
     * @param tasksBetween
     */
    public void setTasksBetween(List<TestAction> tasksBetween) {
        this.tasksBetween = tasksBetween;
    }

    /**
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String beanName) {
        this.name = beanName;
    }

    /**
     * Get the test suite name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the test suite listeners.
     * @param reporter the reporter to set
     */
    public void setTestSuiteListeners(TestSuiteListeners listeners) {
        this.testSuiteListener = listeners;
    }
}
