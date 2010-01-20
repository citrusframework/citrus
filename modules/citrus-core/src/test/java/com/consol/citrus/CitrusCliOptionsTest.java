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

package com.consol.citrus;

import org.apache.commons.cli.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

public class CitrusCliOptionsTest extends AbstractBaseTest {
    
    @Test
    public void testSingleTestArg() throws ParseException {
        CitrusCliOptions options  = new CitrusCliOptions();
        
        CommandLineParser cliParser = new GnuParser();
        CommandLine cmd = cliParser.parse(options, new String[]{"-test", "MyTest"});
        
        Assert.assertEquals(cmd.hasOption("test"), true);
    }
    
    @Test
    public void testMultipleTestArgs() throws ParseException {
        CitrusCliOptions options  = new CitrusCliOptions();
        
        CommandLineParser cliParser = new GnuParser();
        CommandLine cmd = cliParser.parse(options, new String[]{"-test", "MyTest1", "MyTest2"});
        
        Assert.assertEquals(cmd.hasOption("test"), true);
        Assert.assertEquals(cmd.getOptionValues("test").length, 2);
    }
}
