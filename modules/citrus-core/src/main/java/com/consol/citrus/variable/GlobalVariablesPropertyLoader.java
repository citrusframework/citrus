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

public class GlobalVariablesPropertyLoader implements InitializingBean {
    @Autowired
    GlobalVariables globalVariables;
    
    /** list of property files to be loaded as global variables */
    private List<String> propertyFiles = new ArrayList<String>();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalVariablesPropertyLoader.class);
    
    /**
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
                    for (Iterator iter = props.entrySet().iterator(); iter.hasNext();) {
                        Entry entry = (Entry) iter.next();
                        
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
     * @return the propertyFiles
     */
    public List getPropertyFiles() {
        return propertyFiles;
    }

    /**
     * @return the globalVariables
     */
    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    /**
     * @param globalVariables the globalVariables to set
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }
}
