package com.consol.citrus.admin.service;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Single point of access for all admin-gui configuration settings
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.26
 */
@Component
public class ConfigService {
    public static final String PROJECT_HOME = "project.home";

    public File getProjectHome() {
        String projectHomeProperty = System.getProperty(PROJECT_HOME);
        if(projectHomeProperty != null) {
            return new File(projectHomeProperty);
        }
        return null;
    }
}
