/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.util;

import com.consol.citrus.model.spring.beans.Beans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@inheritDoc}
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.20
 */
@Component
public class CitrusConfigHelperImpl implements CitrusConfigHelper {

    protected final static String[] CONTEXT_PATHS = {
            "com.consol.citrus.model.config.core",
            "com.consol.citrus.model.spring.beans"
    };

    @Autowired
    protected JAXBHelper jaxbHelper;

    private JAXBContext jaxbContext;

    @PostConstruct
    protected void initJAXBContext() {
        jaxbContext = jaxbHelper.createJAXBContextByPath(CONTEXT_PATHS);
    }

    public <T> List<T> getConfigElementsByType(Beans beans, Class<T> clazz) {
        List<T> configElements = new ArrayList<T>();

        if (beans.getImportsAndAliasAndBeen() != null) {
            for (Object obj : beans.getImportsAndAliasAndBeen()) {
                if (clazz.isAssignableFrom(obj.getClass())) {
                    configElements.add((T) obj);
                }
            }
        }

        return configElements;
    }

    public boolean deleteConfigElement(Beans beans, Object objectToDelete) {
        if (beans.getImportsAndAliasAndBeen() != null) {
            for (Iterator<Object> iterator = beans.getImportsAndAliasAndBeen().iterator(); iterator.hasNext(); ) {
                if(iterator.next().equals(objectToDelete)) {
                    iterator.remove();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Loads the citrus config for the supplied <code>path</code>.
     *
     * @param path the path to the config file
     * @return the loaded config
     */
    public Beans loadCitrusConfig(File path) {
        return jaxbHelper.unmarshal(jaxbContext, Beans.class, path);
    }

    /**
     * Persists the config, saving it to the supplied <code>path</code> location. If the file already
     * exists, it's overwritten. Otherwise a new file is created.
     *
     * @param path
     * @param jaxbConfig
     */
    public void persistCitrusConfig(File path, Beans jaxbConfig) {
        jaxbHelper.marshal(jaxbContext, jaxbConfig, path);
    }

}
