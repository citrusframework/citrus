package com.consol.citrus.admin.service;

import java.io.File;

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

    /**
     * Get project home from system property.
     * @return
     */
    public File getProjectHome() {
        String projectHomeProperty = System.getProperty(PROJECT_HOME);
        if (projectHomeProperty != null) {
            return new File(projectHomeProperty);
        }
        
        return null;
    }

    /**
     * Sets new project home path.
     * @param homePath
     */
    public void setProjectHome(String homePath) {
        System.setProperty(PROJECT_HOME, homePath);
    }
}
