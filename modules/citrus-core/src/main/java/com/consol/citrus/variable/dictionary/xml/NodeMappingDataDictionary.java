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

package com.consol.citrus.variable.dictionary.xml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.*;

/**
 * Very basic data dictionary that holds a list of mappings for message elements. Mapping key is the element path inside
 * the XML structure {@link com.consol.citrus.util.XMLUtils getNodesPathName()}. The mapping value is set as new element
 * value where test variables are supported in value expressions.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class NodeMappingDataDictionary extends AbstractXmlDataDictionary implements InitializingBean {

    /** Known mappings to this dictionary */
    private Map<String, String> mappings = new HashMap<String, String>();

    /** mapping file resource */
    private Resource mappingFile;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(NodeMappingDataDictionary.class);

    @Override
    public String translate(Node node, String value, TestContext context) {
        String nodePath = XMLUtils.getNodesPathName(node);

        if (getPathMappingStrategy().equals(PathMappingStrategy.EXACT_MATCH)) {
            if (mappings.containsKey(nodePath)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, mappings.get(nodePath)));
                }
                return context.replaceDynamicContentInString(mappings.get(nodePath));
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.endsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
                    return context.replaceDynamicContentInString(entry.getValue());
                }
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.startsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
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
                String key = iter.next().getKey().toString();

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
