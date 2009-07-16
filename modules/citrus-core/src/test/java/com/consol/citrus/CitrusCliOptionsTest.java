package com.consol.citrus;

import org.apache.commons.cli.*;
import org.testng.Assert;
import org.testng.annotations.Test;

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
