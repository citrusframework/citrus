/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.actions;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

import org.citrusframework.citrus.AbstractTestActionBuilder;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

/**
 * Action calls Apache ANT with given build file and runs ANT targets
 * as separate build. User can set and overwrite properties for the build.
 *
 * Build logging output is forwarded to test run logger.
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public class AntRunAction extends AbstractTestAction {

    /** The build.xml file path */
    private final String buildFilePath;

    /** Target to execute */
    private final String target;

    /** Multiple targets to execute as comma separated list */
    private final String targets;

    /** Optional build file properties to set */
    private final Properties properties;

    /** Optional build property file path */
    private final String propertyFilePath;

    /** Custom build listener */
    private final BuildListener buildListener;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AntRunAction.class);

    /**
     * Default constructor.
     * @param builder
     */
    private AntRunAction(Builder builder) {
        super("antrun", builder);

        this.buildFilePath = builder.buildFilePath;
        this.target = builder.target;
        this.targets = builder.targets;
        this.properties = builder.properties;
        this.propertyFilePath = builder.propertyFilePath;
        this.buildListener = builder.buildListener;
    }

    @Override
    public void doExecute(TestContext context) {
        Project project = new Project();
        project.init();

        String buildFileResource = context.replaceDynamicContentInString(buildFilePath);
        try {
            ProjectHelper.configureProject(project, new PathMatchingResourcePatternResolver().getResource(buildFileResource).getFile());

            for (Entry<Object, Object> entry : properties.entrySet()) {
                String propertyValue = entry.getValue() != null ? context.replaceDynamicContentInString(entry.getValue().toString()) : "";
                log.debug("Set build property: " + entry.getKey() + "=" + propertyValue);
                project.setProperty(entry.getKey().toString(), propertyValue);
            }

            loadBuildPropertyFile(project, context);

            if (buildListener != null) {
                project.addBuildListener(buildListener);
            }

            DefaultLogger consoleLogger = new DefaultLogger() {
                @Override
                protected void printMessage(String message, PrintStream stream, int priority) {
                    if (stream.equals(System.err)) {
                        log.error(message);
                    } else {
                        log.info(message);
                    }
                }
            };

            consoleLogger.setErrorPrintStream(System.err);
            consoleLogger.setOutputPrintStream(System.out);
            consoleLogger.setMessageOutputLevel(Project.MSG_DEBUG);

            project.addBuildListener(consoleLogger);

            log.info("Executing ANT build: " + buildFileResource);

            if (StringUtils.hasText(targets)) {
                log.info("Executing ANT targets: " + targets);
                project.executeTargets(parseTargets());
            } else {
                log.info("Executing ANT target: " + target);
                project.executeTarget(target);
            }
        } catch (BuildException e) {
            throw new CitrusRuntimeException("Failed to run ANT build file", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read ANT build file", e);
        }

        log.info("Executed ANT build: " + buildFileResource);
    }

    /**
     * Converts comma delimited string to stack.
     * @return
     */
    private Stack<String> parseTargets() {
        Stack<String> stack = new Stack<String>();
        String[] targetTokens = targets.split(",");

        for (String targetToken : targetTokens) {
            stack.add(targetToken.trim());
        }

        return stack;
    }

    /**
     * Loads build properties from file resource and adds them to ANT project.
     * @param project
     * @param context
     */
    private void loadBuildPropertyFile(Project project, TestContext context) {
        if (StringUtils.hasText(propertyFilePath)) {
            String propertyFileResource = context.replaceDynamicContentInString(propertyFilePath);
            log.info("Reading build property file: " + propertyFileResource);
            Properties fileProperties;
            try {
                fileProperties = PropertiesLoaderUtils.loadProperties(new PathMatchingResourcePatternResolver().getResource(propertyFileResource));

                for (Entry<Object, Object> entry : fileProperties.entrySet()) {
                    String propertyValue = entry.getValue() != null ? context.replaceDynamicContentInString(entry.getValue().toString()) : "";

                    if (log.isDebugEnabled()) {
                        log.debug("Set build property from file resource: " + entry.getKey() + "=" + propertyValue);
                    }
                    project.setProperty(entry.getKey().toString(), propertyValue);
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read build property file", e);
            }
        }
    }

    /**
     * Gets the buildFilePath.
     * @return the buildFilePath the buildFilePath to get.
     */
    public String getBuildFilePath() {
        return buildFilePath;
    }

    /**
     * Gets the target.
     * @return the target the target to get.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the targets.
     * @return the targets the targets to get.
     */
    public String getTargets() {
        return targets;
    }

    /**
     * Gets the properties.
     * @return the properties the properties to get.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets the propertyFilePath.
     * @return the propertyFilePath the propertyFilePath to get.
     */
    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    /**
     * Gets the buildListener.
     * @return the buildListener the buildListener to get.
     */
    public BuildListener getBuildListener() {
        return buildListener;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<AntRunAction, Builder> {

        private String buildFilePath;
        private String target;
        private String targets;
        private Properties properties = new Properties();
        private String propertyFilePath;
        private BuildListener buildListener;

        public static Builder antrun(String buildFilePath) {
            Builder builder = new Builder();
            builder.buildFilePath(buildFilePath);
            return builder;
        }

        /**
         * Sets the build file path.
         * @param buildFilePath
         * @return
         */
        public Builder buildFilePath(String buildFilePath) {
            this.buildFilePath = buildFilePath;
            return this;
        }

        /**
         * Build target name to call.
         * @param target
         */
        public Builder target(String target) {
            this.target = target;
            return this;
        }

        /**
         * Multiple build target names to call.
         * @param targets
         */
        public Builder targets(String ... targets) {
            this.targets = StringUtils.collectionToCommaDelimitedString(Arrays.asList(targets));
            return this;
        }

        /**
         * Adds a build property by name and value.
         * @param name
         * @param value
         */
        public Builder property(String name, Object value) {
            this.properties.put(name, value);
            return this;
        }

        /**
         * Adds build properties.
         * @param properties
         */
        public Builder properties(Properties properties) {
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Adds a build property file reference by file path.
         * @param filePath
         */
        public Builder propertyFile(String filePath) {
            this.propertyFilePath = filePath;
            return this;
        }

        /**
         * Adds custom build listener implementation.
         * @param buildListener
         */
        public Builder listener(BuildListener buildListener) {
            this.buildListener = buildListener;
            return this;
        }

        @Override
        public AntRunAction build() {
            return new AntRunAction(this);
        }
    }

}
