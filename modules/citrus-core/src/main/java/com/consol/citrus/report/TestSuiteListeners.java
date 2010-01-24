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

package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestSuite;

/**
 * Class managing a list of injected test suite listeners. Each event is spread to all
 * managed listeners.
 * 
 * @author Christoph Deppisch
 */
public class TestSuiteListeners implements TestSuiteListener {
    
    /** List of testsuite listeners **/
    @Autowired
    private List<TestSuiteListener> tesSuiteListeners = new ArrayList<TestSuiteListener>();
    
    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish(com.consol.citrus.TestSuite)
     */
    public void onFinish(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinish(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishFailure(testsuite, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess(com.consol.citrus.TestSuite)
     */
    public void onFinishSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishSuccess(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    public void onStart(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStart(testsuite);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartFailure(testsuite, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess(com.consol.citrus.TestSuite)
     */
    public void onStartSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartSuccess(testsuite);
        }
    }
}
