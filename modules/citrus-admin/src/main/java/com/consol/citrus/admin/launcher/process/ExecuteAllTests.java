/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.launcher.process;

import java.io.File;

/**
 * ProcessBuilder for launching all Citrus tests.
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
public class ExecuteAllTests extends ExecuteCommand {

    private static final String MVN_EXECUTE_ALL_TESTS = "mvn surefire:test";

    /**
     * Executes all tests via Maven.
     * @param projectDirectory
     */
    public ExecuteAllTests(File projectDirectory) {
        super(MVN_EXECUTE_ALL_TESTS, projectDirectory);
    }

}
