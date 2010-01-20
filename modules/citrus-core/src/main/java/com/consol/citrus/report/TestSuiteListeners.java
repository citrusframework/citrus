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

public class TestSuiteListeners implements TestSuiteListener {
    
    /** List of testsuite listeners **/
    @Autowired
    private List<TestSuiteListener> tesSuiteListeners = new ArrayList<TestSuiteListener>();
    
    public void onFinish(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinish(testsuite);
        }
    }

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishFailure(testsuite, cause);
        }
    }

    public void onFinishSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onFinishSuccess(testsuite);
        }
    }

    public void onStart(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStart(testsuite);
        }
    }

    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartFailure(testsuite, cause);
        }
    }

    public void onStartSuccess(TestSuite testsuite) {
        for (TestSuiteListener listener : tesSuiteListeners) {
            listener.onStartSuccess(testsuite);
        }
    }
}
