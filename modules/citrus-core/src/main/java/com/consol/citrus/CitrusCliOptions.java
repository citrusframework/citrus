/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Citrus CLI options.
 * 
 * @author Christoph Deppisch
 */
public class CitrusCliOptions extends Options {

    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    @SuppressWarnings("static-access")
    public CitrusCliOptions() {
        
        this.addOption(new Option("help", "print usage help"));
        
        this.addOption(OptionBuilder.withArgName("suitename")
                .hasArg()
                .withDescription("list of testsuites (seperated by blanks) that should run")
                .isRequired(false)
                .create("suitename"));
        
        this.addOption(OptionBuilder.withArgName("test")
                    .hasArgs()
                    .withDescription("list of test (seperated by blanks) that should run")
                    .isRequired(false)
                    .create("test"));
        
        this.addOption(OptionBuilder.withArgName("testdir")
                .hasArg()
                .withDescription("directory to look for test files")
                .isRequired(false)
                .create("testdir"));
        
        this.addOption(OptionBuilder.withArgName("package")
                .hasArgs()
                .withDescription("executes all tests in a package")
                .isRequired(false)
                .create("package"));
    }
}
