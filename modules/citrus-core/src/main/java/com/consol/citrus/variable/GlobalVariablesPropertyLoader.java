/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.variable;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Loads properties from an external property file and creates global test variables.
 * 
 * @author Christoph Deppisch
 */
public class GlobalVariablesPropertyLoader implements InitializingBean {
    @Autowired
    private GlobalVariables globalVariables;
    
    /** List of property files loaded as global variables */
    private List<String> propertyFiles = new ArrayList<String>();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalVariablesPropertyLoader.class);
    
    /**
     * Load the properties as variables.
     * @throws CitrusRuntimeException
     */
    public void loadPropertiesAsVariables() {
        try {
            if (propertyFiles != null && propertyFiles.size() >0) {
                for (String propertyFile: propertyFiles) {

                    Resource file;
                    if (propertyFile.startsWith("classpath:")) {
                        file = new ClassPathResource(propertyFile.substring("classpath:".length()));
                    } else if (propertyFile.startsWith("file:")) {
                        file = new FileSystemResource(propertyFile.substring("file:".length()));
                    } else {
                        file = new FileSystemResource(propertyFile);
                    }

                    log.info("Reading property file " + file.getFilename());
                    Properties props = PropertiesLoaderUtils.loadProperties(file);
                    for (Entry<?, ?> entry : props.entrySet()) {
                        String key = entry.getKey().toString();

                        log.info("Loading property: " + key + "=" + props.getProperty(key) + " into default variables");

                        if (globalVariables.getVariables().containsKey(key) && log.isDebugEnabled()) {
                            log.debug("Overwriting property " + key + " old value:" + globalVariables.getVariables().get(key) + " new value:" + props.getProperty(key));
                        }

                        globalVariables.getVariables().put(key, props.getProperty(key));
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while loading property file", e);
        }
    }
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {
        loadPropertiesAsVariables();
    }
    
    /**
     * Set list of property files to be loaded
     * @param propertyFiles
     */
    public void setPropertyFiles(List<String> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }
    
    /**
     * Get the property files.
     * @return the propertyFiles
     */
    public List<String> getPropertyFiles() {
        return propertyFiles;
    }

    /**
     * Get global variables.
     * @return the globalVariables
     */
    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    /**
     * Set global variables.
     * @param globalVariables the globalVariables to set
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }
}
