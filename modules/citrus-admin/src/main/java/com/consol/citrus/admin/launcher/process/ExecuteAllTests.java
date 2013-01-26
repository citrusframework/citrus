package com.consol.citrus.admin.launcher.process;

import java.io.File;

/**
 * ProcessBuilder for launching a single citrus test.
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
public class ExecuteAllTests extends ExecuteCommand {

    private static final String MVN_EXECUTE_ALL_TESTS = "mvn surefire:test";

    private File projectDirectory;

    public ExecuteAllTests(File projectDirectory) {
        super(MVN_EXECUTE_ALL_TESTS, projectDirectory);
    }

}
