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

package org.citrusframework.citrus.variable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.functions.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * Loads properties from an external property file and creates global test variables.
 *
 * @author Christoph Deppisch
 */
public class GlobalVariablesPropertyLoader implements InitializingBean {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(GlobalVariablesPropertyLoader.class);

    /** Bean name in Spring application context */
    public static final String BEAN_NAME = "globalVariablesPropertyLoader";

    @Autowired
    private GlobalVariables globalVariables;

    @Autowired
    private FunctionRegistry functionRegistry;

    /** List of property files loaded as global variables */
    private List<String> propertyFiles = new ArrayList<>();

    /**
     * Load the properties as variables.
     */
    @Override
    public void afterPropertiesSet() {
        BufferedReader reader = null;

        try {
            if (propertyFilesSet()) {
                for (String propertyFilePath : propertyFiles) {
                    Resource propertyFile = new PathMatchingResourcePatternResolver().getResource(propertyFilePath.trim());

                    LOG.debug("Reading property file " + propertyFile.getFilename());

                    // Use input stream as this also allows to read from resources in a JAR file
                    reader = new BufferedReader(new InputStreamReader(propertyFile.getInputStream()));

                    // local context instance handling variable replacement in property values
                    TestContext context = new TestContext();

                    // Careful!! The function registry *must* be set before setting the global variables.
                    // Variables can contain functions which are resolved when context.setGlobalVariables is invoked.
                    context.setFunctionRegistry(functionRegistry);
                    context.setGlobalVariables(globalVariables);

                    String propertyExpression;
                    while ((propertyExpression = reader.readLine()) != null) {

                        LOG.debug("Property line [ {} ]", propertyExpression);

                        propertyExpression = propertyExpression.trim();
                        if (!isPropertyLine(propertyExpression)) {
                            continue;
                        }

                        String key = propertyExpression.substring(0, propertyExpression.indexOf('=')).trim();
                        String value = propertyExpression.substring(propertyExpression.indexOf('=') + 1).trim();

                        LOG.debug("Property value replace dynamic content [ {} ]", value);
                        value = context.replaceDynamicContentInString(value);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Loading property: " + key + "=" + value + " into default variables");
                        }

                        if (LOG.isDebugEnabled() && globalVariables.getVariables().containsKey(key)) {
                            LOG.debug("Overwriting property " + key + " old value:" + globalVariables.getVariables().get(key)
                                    + " new value:" + value);
                        }

                        globalVariables.getVariables().put(key, value);
                        // we need to keep local context up to date in case of recursive variable usage
                        context.setVariable(key, globalVariables.getVariables().get(key));
                    }

                    LOG.info("Loaded property file " + propertyFile.getFilename());
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while loading property file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.warn("Unable to close property file reader", e);
                }
            }
        }
    }

    private boolean propertyFilesSet() {
        return propertyFiles != null && propertyFiles.size() > 0;
    }

    private boolean isPropertyLine(String line) {
        return StringUtils.hasText(line) && !line.startsWith("#")
                                && line.indexOf('=') > -1;
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
