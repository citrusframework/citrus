/*
 * Copyright the original author or authors.
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

package org.citrusframework.openapi;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * A registry to store objects by OpenApi paths. The registry uses a digital tree data structure
 * that performs path matching with variable placeholders. Variable
 * placeholders must be enclosed in curly braces '{}', e.g., '/api/v1/pet/{id}'. This data structure
 * is optimized for matching paths efficiently, handling both static and dynamic segments.
 * <p>
 * This class is currently not in use but may serve scenarios where a path needs to be mapped to an
 * OasOperation without explicit knowledge of the API to which the path belongs.
 * It could be utilized, for instance, in implementing an OAS message validator based on
 * {@link org.citrusframework.validation.AbstractMessageValidator}.
 */
public class OpenApiPathRegistry<T> {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiPathRegistry.class);

    private final RegistryNode root = new RegistryNode();

    private final Map<String, T> allPaths = new ConcurrentHashMap<>();

    public T search(String path) {
        RegistryNode trieNode = internalSearch(path);
        return trieNode != null ? trieNode.value : null;
    }

    RegistryNode internalSearch(String path) {
        String[] segments = path.split("/");
        return searchHelper(root, segments, 0);
    }

    public boolean insert(String path, T value) {
        return insertInternal(path, value) != null;
    }

    RegistryNode insertInternal(String path, T value) {

        if (path == null || value == null) {
            return null;
        }

        String[] segments = path.split("/");
        RegistryNode node = root;

        if (!allPaths.isEmpty() && (isPathAlreadyContainedWithDifferentValue(path, value)
            || isPathMatchedByOtherPath(path, value))) {
            return null;
        }

        allPaths.put(path, value);
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (builder.isEmpty() || builder.charAt(builder.length() - 1) != '/') {
                builder.append("/");
            }
            builder.append(segment);

            if (!node.children.containsKey(segment)) {
                RegistryNode trieNode = new RegistryNode();
                trieNode.path = builder.toString();
                node.children.put(segment, trieNode);
            }
            node = node.children.get(segment);
        }

        // Sanity check to disallow overwrite of existing values
        if (node.value != null && !node.value.equals(value)) {
            throw new CitrusRuntimeException(format(
                "Illegal attempt to overwrite an existing node value. This is probably a bug. path=%s value=%s",
                node.path, node.value));
        }
        node.value = value;

        return node;
    }

    /**
     * Tests if the path is either matching an existing path or any existing path matches the given
     * patch.
     * <p>
     * For example '/a/b' does not match '/{a}/{b}', but '/{a}/{b}' matches '/a/b'.
     */
    private boolean isPathMatchedByOtherPath(String path, T value) {

        // Does the given path match any existing
        RegistryNode currentValue = internalSearch(path);
        if (currentValue != null && !Objects.equals(path, currentValue.path)) {
            logger.error(
                "Attempt to insert an equivalent path potentially overwriting an existing value. Value for path is ignored: path={}, value={} currentValue={} ",
                path, currentValue, value);
            return true;
        }

        // Does any existing match the path.
        OpenApiPathRegistry<T> tmpTrie = new OpenApiPathRegistry<>();
        tmpTrie.insert(path, value);

        List<String> allMatching = allPaths.keySet().stream()
            .filter(existingPath -> {
                RegistryNode trieNode = tmpTrie.internalSearch(existingPath);
                return trieNode != null && !existingPath.equals(trieNode.path);
            }).map(existingPath -> "'" + existingPath + "'").toList();
        if (!allMatching.isEmpty() && logger.isErrorEnabled()) {
            logger.error(
                "Attempt to insert an equivalent path overwritten by existing paths. Value for path is ignored: path={}, value={} existingPaths=[{}]",
                path, currentValue, String.join(",", allMatching));

        }

        return !allMatching.isEmpty();
    }

    private boolean isPathAlreadyContainedWithDifferentValue(String path, T value) {
        T currentValue = allPaths.get(path);
        if (currentValue != null) {
            if (value.equals(currentValue)) {
                return false;
            }
            logger.error(
                "Attempt to overwrite value for path is ignored: path={}, value={} currentValue={} ",
                path, currentValue, value);
            return true;
        }
        return false;
    }

    private RegistryNode searchHelper(RegistryNode node, String[] segments, int index) {
        if (node == null) {
            return null;
        }
        if (index == segments.length) {
            return node;
        }

        String segment = segments[index];

        // Exact match
        if (node.children.containsKey(segment)) {
            RegistryNode foundNode = searchHelper(node.children.get(segment), segments, index + 1);
            if (foundNode != null && foundNode.value != null) {
                return foundNode;
            }
        }

        // Variable match
        for (String key : node.children.keySet()) {
            if (key.startsWith("{") && key.endsWith("}")) {
                RegistryNode foundNode = searchHelper(node.children.get(key), segments, index + 1);
                if (foundNode != null && foundNode.value != null) {
                    return foundNode;
                }
            }
        }

        return null;
    }

    class RegistryNode {
        Map<String, RegistryNode> children = new HashMap<>();
        String path;
        T value = null;
    }
}
