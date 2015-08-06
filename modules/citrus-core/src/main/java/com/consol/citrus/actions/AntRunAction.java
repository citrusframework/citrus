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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.tools.ant.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;

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
    private String buildFilePath;
    
    /** Target to execute */
    private String target;
    
    /** Multiple targets to execute as comma separated list */
    private String targets;
    
    /** Optional build file properties to set */
    private Properties properties = new Properties();
    
    /** Optional build property file path */
    private String propertyFilePath;
    
    /** Custom build listener */
    private BuildListener buildListener;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AntRunAction.class);

    /**
     * Default constructor.
     */
    public AntRunAction() {
        setName("antrun");
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
            
            log.info("Running ANT build file: " + buildFileResource);
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
        
        log.info("ANT build run successful");
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
                    log.debug("Set build property from file resource: " + entry.getKey() + "=" + propertyValue);
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
     * Sets the buildFilePath.
     * @param buildFilePath the buildFilePath to set
     */
    public AntRunAction setBuildFilePath(String buildFilePath) {
        this.buildFilePath = buildFilePath;
        return this;
    }

    /**
     * Gets the target.
     * @return the target the target to get.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target.
     * @param target the target to set
     */
    public AntRunAction setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Gets the targets.
     * @return the targets the targets to get.
     */
    public String getTargets() {
        return targets;
    }

    /**
     * Sets the targets.
     * @param targets the targets to set
     */
    public AntRunAction setTargets(String targets) {
        this.targets = targets;
        return this;
    }

    /**
     * Gets the properties.
     * @return the properties the properties to get.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the properties.
     * @param properties the properties to set
     */
    public AntRunAction setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Gets the propertyFilePath.
     * @return the propertyFilePath the propertyFilePath to get.
     */
    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    /**
     * Sets the propertyFilePath.
     * @param propertyFilePath the propertyFilePath to set
     */
    public AntRunAction setPropertyFilePath(String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;
        return this;
    }

    /**
     * Gets the buildListener.
     * @return the buildListener the buildListener to get.
     */
    public BuildListener getBuildListener() {
        return buildListener;
    }

    /**
     * Sets the buildListener.
     * @param buildListener the buildListener to set
     */
    public AntRunAction setBuildListener(BuildListener buildListener) {
        this.buildListener = buildListener;
        return this;
    }

}
