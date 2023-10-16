/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.xml.namespace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

public class DefaultNamespaceContext implements NamespaceContext {

    private final Map<String, String> namespaces = new HashMap<>();

    /**
     * Adds given namespace mappings to this context.
     * @param mappings
     */
    public void addNamespaces(Map<String, String> mappings) {
        this.namespaces.putAll(mappings);
    }

    public void addNamespace(String prefix, String namespaceUri) {
        this.namespaces.put(prefix, namespaceUri);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return this.namespaces.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            return null;
        }

        return this.namespaces.entrySet().stream()
                .filter(entry -> namespaceURI.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return this.namespaces.entrySet().stream()
                .filter(entry -> namespaceURI.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList()
                .iterator();
    }
}
