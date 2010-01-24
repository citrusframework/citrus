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

import com.consol.citrus.TestSuite;

/**
 * Listener for events regarding a test suite (start, finish, failure, success)
 * @author Christoph Deppisch
 */
public interface TestSuiteListener {

    /**
     * Invoked on test suite start.
     * @param testsuite
     */
    public void onStart(TestSuite testsuite);

    /**
     * Invoked after successful test suite start.
     * @param testsuite
     */
    public void onStartSuccess(TestSuite testsuite);

    /**
     * Invoked after failed test suite start.
     * @param testsuite
     * @param cause
     */
    public void onStartFailure(TestSuite testsuite, Throwable cause);

    /**
     * Invoked on test suite finish.
     * @param testsuite
     */
    public void onFinish(TestSuite testsuite);

    /**
     * Invoked after successful test suite finish.
     * @param testsuite
     */
    public void onFinishSuccess(TestSuite testsuite);

    /**
     * Invoked after failed test suite finish.
     * @param testsuite
     * @param cause
     */
    public void onFinishFailure(TestSuite testsuite, Throwable cause);
}
