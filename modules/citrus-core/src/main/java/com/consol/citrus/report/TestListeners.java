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

public class TestListeners implements TestListener {
    
    /** List of test listeners **/
    @Autowired
    private List<TestListener> testListeners = new ArrayList<TestListener>();
    
    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestListener listener : testListeners) {
            listener.onTestFailure(test, cause);
        }
    }

    public void onTestFinish(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestFinish(test);
        }
    }

    public void onTestSkipped(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSkipped(test);
        }
    }

    public void onTestStart(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestStart(test);
        }
    }

    public void onTestSuccess(TestCase test) {
        for (TestListener listener : testListeners) {
            listener.onTestSuccess(test);
        }
    }
}
