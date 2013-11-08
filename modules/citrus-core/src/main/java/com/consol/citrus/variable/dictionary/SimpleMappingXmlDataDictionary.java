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

package com.consol.citrus.variable.dictionary;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.*;

/**
 * Very basic data dictionary that holds a list of mappings for message elements. Mapping key is the element path inside
 * the XML structure {@link com.consol.citrus.util.XMLUtils getNodesPathName()}. The mapping value is set as new element
 * value where test variables are supported in value expressions.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class SimpleMappingXmlDataDictionary extends AbstractXmlDataDictionary implements InitializingBean {

    /** Known mappings to this dictionary */
    private Map<String, String> mappings = new HashMap<String, String>();

    /** mapping file resource */
    private Resource mappingFile;

    /** Kind of mapping strategy how to identify dictionary item */
    private PathMappingStrategy pathMappingStrategy = PathMappingStrategy.EXACT_MATCH;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SimpleMappingXmlDataDictionary.class);

    @Override
    public String translate(String value, String path, TestContext context) {
        if (pathMappingStrategy.equals(PathMappingStrategy.EXACT_MATCH)) {
            if (mappings.containsKey(path)) {
                return context.replaceDynamicContentInString(mappings.get(path));
            }
        } else if (pathMappingStrategy.equals(PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (path.endsWith(entry.getKey())) {
                    return context.replaceDynamicContentInString(entry.getValue());
                }
            }
        } else if (pathMappingStrategy.equals(PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    return context.replaceDynamicContentInString(entry.getValue());
                }
            }
        }

        return value;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mappingFile != null) {

            log.info("Reading mapping file " + mappingFile.getFilename());
            Properties props;
            try {
                props = PropertiesLoaderUtils.loadProperties(mappingFile);
            } catch (IOException e) {
                throw new CitrusRuntimeException(e);
            }

            for (Iterator<Map.Entry<Object, Object>> iter = props.entrySet().iterator(); iter.hasNext();) {
                String key = ((Map.Entry<Object, Object>)iter.next()).getKey().toString();

                log.info("Loading mapping: " + key + "=" + props.getProperty(key));

                if (log.isDebugEnabled() && mappings.containsKey(key)) {
                    log.debug("Overwriting mapping " + key + " old value:" + mappings.get(key)
                            + " new value:" + props.getProperty(key));
                }

                mappings.put(key, props.getProperty(key));
            }
        }
    }

    /**
     * Sets the mappings.
     * @param mappings
     */
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Sets the path mapping strategy.
     * @param pathMappingStrategy
     */
    public void setPathMappingStrategy(PathMappingStrategy pathMappingStrategy) {
        this.pathMappingStrategy = pathMappingStrategy;
    }

    /**
     * Gets the mapping file resource.
     * @return
     */
    public Resource getMappingFile() {
        return mappingFile;
    }

    /**
     * Sets the mapping file resource.
     * @param mappingFile
     */
    public void setMappingFile(Resource mappingFile) {
        this.mappingFile = mappingFile;
    }
}
