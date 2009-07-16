package com.consol.citrus;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class CitrusCliOptions extends Options {
    @SuppressWarnings("static-access")
    public CitrusCliOptions() {
        
        this.addOption(new Option("help", "print usage help"));
        
        this.addOption(OptionBuilder.withArgName("testsuites")
                .hasArgs()
                .withDescription("list of testsuites (seperated by blanks) that should run")
                .isRequired(false)
                .create("testsuite"));
        
        this.addOption(OptionBuilder.withArgName("testnames")
                    .hasArgs()
                    .withDescription("list of test (seperated by blanks) that should run")
                    .isRequired(false)
                    .create("test"));
        
        this.addOption(OptionBuilder.withArgName("directory")
                .hasArg()
                .withDescription("directory to look for test files")
                .isRequired(false)
                .create("testdir"));
    }
}
