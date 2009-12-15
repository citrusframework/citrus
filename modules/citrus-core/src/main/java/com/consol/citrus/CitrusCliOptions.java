/*
 * Copyright 2006-2009 ConSol* Software GmbH.
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

package com.consol.citrus;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class CitrusCliOptions extends Options {

    private static final long serialVersionUID = 1L;

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
