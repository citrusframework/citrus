/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.xml.namespace;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;

/**
 * @author Christoph Deppisch
 */
public class CitrusNamespacePrefixMapper extends NamespacePrefixMapper {

    /** List of known namespaces with mapping to prefix */
    private Map<String, String> namespaceMappings = new HashMap<>();

    public CitrusNamespacePrefixMapper() {
        namespaceMappings.put("http://www.citrusframework.org/schema/testcase", "");
        namespaceMappings.put("http://www.citrusframework.org/schema/http/testcase", "http");
        namespaceMappings.put("http://www.citrusframework.org/schema/ws/testcase", "ws");
        namespaceMappings.put("http://www.citrusframework.org/schema/docker/testcase", "docker");
        namespaceMappings.put("http://www.citrusframework.org/schema/kubernetes/testcase", "k8s");
        namespaceMappings.put("http://www.citrusframework.org/schema/selenium/testcase", "selenium");
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if (namespaceMappings.containsKey(namespaceUri)) {
            return namespaceMappings.get(namespaceUri);
        }

        return suggestion;
    }

    /**
     * Gets the namespace mappings.
     * @return
     */
    public Map<String, String> getNamespaceMappings() {
        return namespaceMappings;
    }

    /**
     * Sets the namespace mappings.
     * @param namespaceMappings
     */
    public void setNamespaceMappings(Map<String, String> namespaceMappings) {
        this.namespaceMappings = namespaceMappings;
    }
}
