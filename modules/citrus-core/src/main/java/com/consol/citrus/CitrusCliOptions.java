package com.consol.citrus;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class CitrusCliOptions extends Options {
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
