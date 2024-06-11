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

package org.citrusframework.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.Named;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * Base class for repositories providing common functionality for initializing and managing resources.
 * Implementations must provide the logic for loading and adding resources to the repository.
 */
public abstract class BaseRepository implements Named, InitializingPhase {

    private String name;

    /** List of location patterns that will be translated to schema resources */
    private List<String> locations = new ArrayList<>();

    protected BaseRepository(String name) {
        this.name = name;
    }

    @Override
    public void initialize() {
        try {
            ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
            for (String location : locations) {
                Resource found = Resources.create(location);
                if (found.exists()) {
                    addRepository(found);
                } else {
                    Set<Path> findings;
                    if (StringUtils.hasText(FileUtils.getFileExtension(location))) {
                        String fileNamePattern = FileUtils.getFileName(location).replace(".", "\\.").replace("*", ".*");
                        String basePath = FileUtils.getBasePath(location);
                        findings = resourceResolver.getResources(basePath, fileNamePattern);
                    } else {
                        findings = resourceResolver.getResources(location);
                    }

                    for (Path resource : findings) {
                        addRepository(Resources.fromClasspath(resource.toString()));
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to initialize repository", e);
        }
    }

    protected abstract void addRepository(Resource resource);

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     * @return the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the locations.
     * @return the locations to get.
     */
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Sets the locations.
     * @param locations the locations to set
     */
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

}
