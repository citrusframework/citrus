/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.report;

import com.consol.citrus.TestCase;

public interface TestListener {
    /**
     * Invoked when test gets started
     * @param test
     */
    public void onTestStart(TestCase test);

    /**
     * Invoked when test gets finished
     * @param test
     */
    public void onTestFinish(TestCase test);

    /**
     * Invoked when test finished with success
     * @param test
     */
    public void onTestSuccess(TestCase test);

    /**
     * Invoked when test finished with failure
     * @param test
     */
    public void onTestFailure(TestCase test, Throwable cause);

    /**
     * Invoked when test is skipped
     * @param test
     */
    public void onTestSkipped(TestCase test);
}
