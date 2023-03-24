/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.junit;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public final class TestSuiteState {

    /** Boolean state holder, true indicates that before suite has already been executed */
    private static Boolean beforeSuiteState = false;

    /**
     * Prevent instantiation.
     */
    private TestSuiteState() {
    }

    /**
     * Perform synchronized checks on before suite already done. If before suite has not been done yet
     * set before suite flag and return true to indicate that before suite actions should be executed.
     * Else return false as before suite actions already have been executed in some earlier state.
     *
     * @return flag indicating if before suite should be executed
     */
    public static synchronized boolean shouldExecuteBeforeSuite() {
        if (!beforeSuiteState) {
            beforeSuiteState = true;
            return true;
        } else {
            return false;
        }
    }
}
