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

package com.consol.citrus.admin.launcher.process.maven;

import com.consol.citrus.admin.configuration.MavenRunConfiguration;

import java.io.File;

/**
 * ProcessBuilder for launching a single citrus test.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3
 */
public class MavenRunSingleTestCommand extends MavenCommand {

    /** Test to execute */
    private String testName;

    public MavenRunSingleTestCommand(File projectDirectory, String testName, MavenRunConfiguration configuration) {
        super(MavenCommand.COMPILE + MavenCommand.TEST, projectDirectory, configuration);
        this.testName = testName;
    }

    protected void prepareConfiguration(MavenRunConfiguration configuration) {
        configuration.getSystemProperties().put("test", testName);
    }
}
