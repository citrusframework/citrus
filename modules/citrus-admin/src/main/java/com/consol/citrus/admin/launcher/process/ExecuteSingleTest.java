package com.consol.citrus.admin.launcher.process;

import java.io.File;

/**
 * ProcessBuilder for launching a single citrus test.
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
public class ExecuteSingleTest extends ExecuteCommand {

    private static final String MVN_EXECUTE_SINGLE_TEST = "mvn surefire:test -Dtest=%s";

    public ExecuteSingleTest(File projectDirectory, String testName) {
        super(String.format(MVN_EXECUTE_SINGLE_TEST, testName), projectDirectory);
    }
}
