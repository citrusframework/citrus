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

import com.consol.citrus.TestCase;

/**
 * Class that spreads test events to all available test listeners 
 * injected by Spring's IoC container.
 * 
 * @author Christoph Deppisch
 */
public class TestListeners implements TestListener {
    
    /** List of test listeners **/
    @Autowired
    private List<TestListener> testListeners = new ArrayList<TestListener>();
   
    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestListener listener : testListeners) {
            listener.onTestFailure(test, cause);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestFinish(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSkipped(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestStart(test);
        }
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSuccess(test);
        }
    }
}
