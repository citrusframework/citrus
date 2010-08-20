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

package com.consol.citrus.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action reads property files and creates test variables for every property entry. File
 * resource path can define a {@link ClassPathResource} or a {@link FileSystemResource}.
 * 
 * @author Christoph Deppisch
 */
public class LoadPropertiesAction extends AbstractTestAction {

    /** File resource path */
    private String file = null;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(LoadPropertiesAction.class);

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        Resource resource;
        if (file.startsWith("classpath:")) {
            resource = new ClassPathResource(file.substring("classpath:".length()));
        } else if (file.startsWith("file:")) {
            resource = new FileSystemResource(file.substring("file:".length()));
        } else {
            resource = new FileSystemResource(file);
        }

        log.info("Reading property file " + resource.getFilename());
        Properties props;
        try {
            props = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        for (Iterator<Entry<Object, Object>> iter = props.entrySet().iterator(); iter.hasNext();) {
            String key = ((Entry<Object, Object>)iter.next()).getKey().toString();

            log.info("Loading property: " + key + "=" + props.getProperty(key) + " into variables");

            if (log.isDebugEnabled() && context.getVariables().containsKey(key)) {
                log.debug("Overwriting property " + key + " old value:" + context.getVariable(key) 
                        + " new value:" + props.getProperty(key));
            }

            try {
                context.setVariable(key, context.replaceDynamicContentInString(props.getProperty(key)));
            } catch (ParseException e) {
                throw new CitrusRuntimeException("Failed to resolve value for property '" + key + "'", e);
            }
        }
    }

    /**
     * File path setter.
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }
}
