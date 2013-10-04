/*
 * Copyright 2006-2013 the original author or authors.
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
import com.consol.citrus.admin.configuration.ClasspathRunConfiguration;
import com.consol.citrus.admin.configuration.MavenRunConfiguration;
import com.consol.citrus.admin.configuration.RunConfiguration;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single point of access for all configuration settings. These are project related settings like project home and
 * root directory information.
 *
 * @author Martin Maher, Christoph Deppisch
 * @since 1.3
 */
@Component
public class ConfigurationService {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private FileHelper fileHelper;
    
    /** Preferences fields */
    private String projectHome = System.getProperty(PROJECT_HOME, "");
    private String rootDirectory = System.getProperty(ROOT_DIRECTORY, System.getProperty("user.home"));
    private String basePackage = System.getProperty(BASE_PACKAGE, "com.consol.citrus");

    /** System property names */
    public static final String PROJECT_HOME = "project.home";
    public static final String ROOT_DIRECTORY = "root.directory";
    /** Base package for test cases to look for */
    public static final String BASE_PACKAGE = "test.base.package";

    /** List of run configurations */
    private List<RunConfiguration> runConfigurationList = new ArrayList<RunConfiguration>();

    public ConfigurationService() {
        MavenRunConfiguration mavenRunConfiguration = new MavenRunConfiguration();
        mavenRunConfiguration.setId("Maven");
        runConfigurationList.add(mavenRunConfiguration);

        ClasspathRunConfiguration classpathRunConfiguration = new ClasspathRunConfiguration();
        classpathRunConfiguration.setId("Classpath");
        runConfigurationList.add(classpathRunConfiguration);
    }

    public RunConfiguration getRunConfiguration(String id) {
        for (RunConfiguration runConfiguration : runConfigurationList) {
            if (runConfiguration.getId().equals(id)) {
                return runConfiguration;
            }
        }

        throw new CitrusAdminRuntimeException("Unknown run configuration: " + id);
    }

    public List<RunConfiguration> getRunConfigurations() {
        return runConfigurationList;
    }
    
    /**
     * Check if home directory is valid Citrus project home.
     *
     * @param directory
     */
    public boolean isProjectHome(String directory) {
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
     * Returns the project's config file. It's assumed that there is only a single config file
     * within the project and that it's named 'citrus-admin-context.xml'.
     *
     * @return the config file or null if no config file exists within the selected project.
     */
    public File getProjectConfigFile() {
        return fileHelper.findFileInPath(new File(getProjectHome()), CitrusConstants.DEFAULT_APPLICATION_CONTEXT, true);
    }
    
    /**
     * Get project home from system property.
     * @return
     */
    public String getProjectHome() {
        if (StringUtils.hasText(projectHome)) {
            try {
                return new File(projectHome).getCanonicalPath();
            } catch (IOException e) {
                throw new CitrusAdminRuntimeException("Unable to access project home directory", e);
            }
        } else {
            return "";
        }
    }
    
    /**
     * Sets new project home path.
     * @param projectHome
     */
    public void setProjectHome(String projectHome) {
        this.projectHome = projectHome;
        System.setProperty(PROJECT_HOME, projectHome);
    }
    
    /**
     * Gets the root directory from system property. By default user.home system
     * property setting is used as root.
     * @return
     */
    public String getRootDirectory() {
        return rootDirectory;
    }
    
    /**
     * Sets the rootDirectory.
     * @param rootDirectory
     */
    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
        System.setProperty(ROOT_DIRECTORY, rootDirectory);
    }

    /**
     * Gets the current base package for tests.
     * @return
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * Sets the test base package.
     * @param basePackage
     */
    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        System.setProperty(BASE_PACKAGE, basePackage);
    }
}
