package com.consol.citrus.admin.launcher.process;

import java.io.File;

/**
 * ProcessBuilder for launching a single citrus test.
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
public class RebuildProject extends ExecuteCommand {

    private static final String MVN_REBUILD = "mvn clean install -Dtest -DfailIfNoTests=false";

    public RebuildProject(File projectDirectory) {
        super(MVN_REBUILD, projectDirectory);
    }
}
