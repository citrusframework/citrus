package com.consol.citrus.admin.launcher.process.maven;

import com.consol.citrus.admin.configuration.MavenRunConfiguration;
import com.consol.citrus.admin.launcher.process.ExecuteCommand;

import java.io.File;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MavenCommand extends ExecuteCommand {

    protected static final String MVN = "mvn ";
    protected static final String COMPILE = "compile ";
    protected static final String TEST = "surefire:test ";
    protected static final String CLEAN = "clean ";
    protected static final String INSTALL = "install ";

    private MavenRunConfiguration configuration;

    /**
     * Constructor for executing a command.
     *
     * @param command          the command to be executed
     */
    public MavenCommand(String command, File workingDirectory, MavenRunConfiguration configuration) {
        super(command, workingDirectory);

        this.configuration = configuration;
    }

    protected String buildCommand(String command) {
        StringBuilder builder = new StringBuilder();

        builder.append(MVN);
        builder.append(command);

        if (configuration == null) {
            configuration = new MavenRunConfiguration();
        }

        prepareConfiguration(configuration);

        for (Map.Entry<Object, Object> propertyEntry: configuration.getSystemProperties().entrySet()) {
            builder.append(String.format("-D%s=%s ", propertyEntry.getKey(), propertyEntry.getValue()));
        }

        for (String profile: configuration.getProfiles()) {
            builder.append(String.format("-P%s ", profile));
        }

        return builder.toString();
    }

    protected void prepareConfiguration(MavenRunConfiguration configuration) {
    }
}
