/*
 * Copyright 2006-2010 the original author or authors.
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
