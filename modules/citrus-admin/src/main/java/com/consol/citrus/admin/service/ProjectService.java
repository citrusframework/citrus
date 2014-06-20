/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.service;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.admin.configuration.PropertyConstants;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.Project;
import com.consol.citrus.admin.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
@Component
public class ProjectService implements InitializingBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private FileHelper fileHelper;

    @Autowired
    private TestCaseService testCaseService;

    /** Current project actively opened in Citrus admin */
    private Project project;

    /**
     * Loads Citrus project from project home.
     * @param projectHomeDir
     * @return
     */
    public void load(String projectHomeDir) {
        if (!validateProjectHome(projectHomeDir)) {
            throw new CitrusAdminRuntimeException("Invalid project home - not a proper Citrus project");
        }

        System.setProperty(PropertyConstants.PROJECT_HOME, projectHomeDir);
        project = new Project(projectHomeDir);
        project.setup();

        project.setTestCount(testCaseService.getTestCount(project));
    }

    /**
     * Returns the project's Spring application context config file.
     * @return the config file or null if no config file exists within the selected project.
     */
    public File getProjectContextConfigFile() {
        return fileHelper.findFileInPath(new File(project.getProjectHome()), CitrusConstants.DEFAULT_APPLICATION_CONTEXT, true);
    }

    /**
     * Reads default Citrus project property file for active project.
     * @return properties loaded or empty properties if nothing is found
     */
    public Properties getProjectProperties() {
        String defaultPropertyFilePath = getActiveProject().getProjectHome() + "/src/citrus/resources/citrus.properties";
        FileSystemResource defaultPropertyFile = new FileSystemResource(defaultPropertyFilePath);

        try {
            return PropertiesLoaderUtils.loadProperties(defaultPropertyFile);
        } catch (IOException e) {
            log.warn(String. format("Unable to read default Citrus project properties from file resource '%s'", defaultPropertyFilePath));
        }

        return new Properties();
    }

    /**
     * Checks if home directory is valid Citrus project home.
     *
     * @param directory
     */
    private boolean validateProjectHome(String directory) {
        File homeDir = new File(directory);

        try {
            Assert.isTrue(homeDir.exists());
            Assert.isTrue(new File(homeDir, "src/citrus").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/resources").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/resources/citrus-context.xml").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/tests").exists());
            Assert.isTrue(new File(homeDir, "src/citrus/java").exists());
        } catch (IllegalArgumentException e) {
            log.warn("Project home validation failed", e);
            return false;
        }

        return true;
    }

    /**
     * Gets the currently active project.
     * @return
     */
    public Project getActiveProject() {
        return project;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (project == null && StringUtils.hasText(System.getProperty(PropertyConstants.PROJECT_HOME))) {
            load(System.getProperty(PropertyConstants.PROJECT_HOME));
        }
    }
}
