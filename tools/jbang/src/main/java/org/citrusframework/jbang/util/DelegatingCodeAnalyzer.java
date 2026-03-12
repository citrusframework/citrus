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

package org.citrusframework.jbang.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.FileUtils;

/**
 * Delegates to the proper code analyzer implementation based on the given file extension.
 */
public class DelegatingCodeAnalyzer implements CodeAnalyzer {

    @Override
    public ScanResult scan(String fileName, String code) throws IOException {
        String ext = FileUtils.getFileExtension(fileName);

        return switch (ext) {
            case "yaml", "yml" -> new YamlCodeAnalyzer().scan(fileName, code);
            default -> throw new CitrusRuntimeException("Failed to analyze code for the file type: %s".formatted(ext));
        };
    }

    @Override
    public Set<String> scanModules(String code) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> scanDependencies(String code) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> scanTestActions(String code, Set<String> modules) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> scanTestContainers(String code, Set<String> modules) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> scanEndpoints(String code, Set<String> modules) {
        return Collections.emptySet();
    }
}
