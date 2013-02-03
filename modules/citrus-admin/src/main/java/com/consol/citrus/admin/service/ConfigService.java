package com.consol.citrus.admin.service;

import org.springframework.stereotype.Component;

/**
 * Single point of access for all configuration settings
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
@Component
public class ConfigService {
    /** System property name for project home setting */
    public static final String PROJECT_HOME = "project.home";
    
    public static final String ROOT_DIRECTORY = "root.directory";

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
