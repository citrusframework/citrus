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

package com.consol.citrus.actions;

import java.io.IOException;
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

public class LoadPropertiesAction extends AbstractTestAction {

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

            if (context.getVariables().containsKey(key) && log.isDebugEnabled()) {
                log.debug("Overwriting property " + key + " old value:" + context.getVariable(key) + " new value:" + props.getProperty(key));
            }

            context.setVariable(key, props.getProperty(key));
        }
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }
}
