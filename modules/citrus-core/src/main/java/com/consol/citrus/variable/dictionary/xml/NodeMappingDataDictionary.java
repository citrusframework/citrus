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
import com.consol.citrus.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Very basic data dictionary that holds a list of mappings for message elements. Mapping key is the element path inside
 * the XML structure {@link com.consol.citrus.util.XMLUtils getNodesPathName()}. The mapping value is set as new element
 * value where test variables are supported in value expressions.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class NodeMappingDataDictionary extends AbstractXmlDataDictionary implements InitializingBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(NodeMappingDataDictionary.class);

    @Override
    public <T> T translate(Node node, T value, TestContext context) {
        String nodePath = XMLUtils.getNodesPathName(node);

        if (getPathMappingStrategy().equals(PathMappingStrategy.EXACT)) {
            if (mappings.containsKey(nodePath)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, mappings.get(nodePath)));
                }
                return convertIfNecessary(context.replaceDynamicContentInString(mappings.get(nodePath)), value);
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.endsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
                    return convertIfNecessary(context.replaceDynamicContentInString(entry.getValue()), value);
                }
            }
        } else if (getPathMappingStrategy().equals(PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.startsWith(entry.getKey())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
                    return convertIfNecessary(context.replaceDynamicContentInString(entry.getValue()), value);
                }
            }
        }

        return value;
    }
}
