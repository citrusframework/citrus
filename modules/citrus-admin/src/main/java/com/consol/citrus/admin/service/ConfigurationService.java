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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.consol.citrus.admin.util.FileHelper;

/**
 * Single point of access for all configuration settings. These are project related activities like project home selection and
 * project specific settings.
 *
 * @author Martin Maher, Christoph Deppisch
 */
@Component
public class ConfigurationService {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(ConfigurationService.class);
    
    @Autowired
    private FileHelper fileHelper;
    
    /** System property names */
    public static final String PROJECT_HOME = "project.home";
    public static final String ROOT_DIRECTORY = "root.directory";
    
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
        final String configFilename = "citrus-admin-context.xml";
        final File projectHome = new File(getProjectHome());
        return fileHelper.findFileInPath(projectHome, configFilename, true);
    }
    
    /**
     * Get project home from system property.
     * @return
     */
    public String getProjectHome() {
        return System.getProperty(PROJECT_HOME);
    }
    
    /**
     * Gets the root directory from system property. By default user.home system
     * property setting is used as root.
     * @return
     */
    public String getRootDirectory() {
        return System.getProperty(ROOT_DIRECTORY, System.getProperty("user.home"));
    }

    /**
     * Sets new project home path.
     * @param homePath
     */
    public void setProjectHome(String homePath) {
        System.setProperty(PROJECT_HOME, homePath);
    }
    
}
