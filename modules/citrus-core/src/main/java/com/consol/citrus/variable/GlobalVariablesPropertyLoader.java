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

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.util.FileUtils;

/**
 * Loads properties from an external property file and creates global test variables.
 * 
 * @author Christoph Deppisch
 */
public class GlobalVariablesPropertyLoader implements InitializingBean {
    @Autowired
    private GlobalVariables globalVariables;
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
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
        BufferedReader reader = null;
        
        try {
            if (propertyFiles != null && propertyFiles.size() >0) {
                for (String propertyFile: propertyFiles) {

                    Resource file = FileUtils.getResourceFromFilePath(propertyFile);

                    log.info("Reading property file " + file.getFilename());
                    reader = new BufferedReader(new FileReader(file.getFile()));
                    
                    // local context instance handling variable replacement in property values
                    TestContext context = new TestContext();
                    context.setGlobalVariables(globalVariables);
                    context.setFunctionRegistry(functionRegistry);
                    
                    String propertyExpression;
                    while ((propertyExpression = reader.readLine()) != null) {
                        propertyExpression = propertyExpression.trim();
                        if(!StringUtils.hasText(propertyExpression) || propertyExpression.startsWith("#") 
                                || propertyExpression.indexOf("=") == -1) {
                            continue;
                        }
                        
                        String key = propertyExpression.substring(0, propertyExpression.indexOf("=")).trim();
                        String value = propertyExpression.substring(propertyExpression.indexOf("=")+1).trim();

                        try {
                            value = context.replaceDynamicContentInString(value);
                        } catch (ParseException e) {
                            throw new CitrusRuntimeException("Error while parsing global variables. " +
                                    "Can not resolve property '" + key + "'", e);
                        }
                        
                        log.info("Loading property: " + key + "=" + value + " into default variables");

                        if (log.isDebugEnabled() && globalVariables.getVariables().containsKey(key)) {
                            log.debug("Overwriting property " + key + " old value:" + globalVariables.getVariables().get(key) 
                                    + " new value:" + value);
                        }
                        
                        globalVariables.getVariables().put(key, value);
                        // we need to keep local context up to date in case of recursive variable usage
                        context.setVariable(key, globalVariables.getVariables().get(key));
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while loading property file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Unable to close property file reader", e);
                }
            }
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
     * Set the global variables.
     * @param globalVariables the globalVariables to set
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }

    /**
     * Set the function registry.
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}
