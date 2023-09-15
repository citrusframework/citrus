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

package org.citrusframework.variable.dictionary.xml;

import java.util.Map;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Very basic data dictionary that holds a list of mappings for message elements. Mapping key is the element path inside
 * the XML structure {@link org.citrusframework.util.XMLUtils getNodesPathName()}. The mapping value is set as new element
 * value where test variables are supported in value expressions.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class NodeMappingDataDictionary extends AbstractXmlDataDictionary implements InitializingPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(NodeMappingDataDictionary.class);

    @Override
    public <T> T translate(Node node, T value, TestContext context) {
        String nodePath = XMLUtils.getNodesPathName(node);

        if (getPathMappingStrategy().equals(DataDictionary.PathMappingStrategy.EXACT)) {
            if (mappings.containsKey(nodePath)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, mappings.get(nodePath)));
                }
                return convertIfNecessary(mappings.get(nodePath), value, context);
            }
        } else if (getPathMappingStrategy().equals(DataDictionary.PathMappingStrategy.ENDS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.endsWith(entry.getKey())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
                    return convertIfNecessary(entry.getValue(), value, context);
                }
            }
        } else if (getPathMappingStrategy().equals(DataDictionary.PathMappingStrategy.STARTS_WITH)) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                if (nodePath.startsWith(entry.getKey())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Data dictionary setting element '%s' with value: %s", nodePath, entry.getValue()));
                    }
                    return convertIfNecessary(entry.getValue(), value, context);
                }
            }
        }

        return value;
    }
}
