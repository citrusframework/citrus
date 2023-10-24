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

package org.citrusframework.json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.Named;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema repository holding a set of json schema resources known in the test scope.
 * @since 2.7.3
 */
public class JsonSchemaRepository  implements Named, InitializingPhase {

    /** This repositories name in the Spring application context */
    private String name;

    /** List of schema resources */
    private List<SimpleJsonSchema> schemas = new ArrayList<>();

    /** List of location patterns that will be translated to schema resources */
    private List<String> locations = new ArrayList<>();

    /** Logger */
    private static Logger logger = LoggerFactory.getLogger(JsonSchemaRepository.class);

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void initialize() {
        try {
            ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
            for (String location : locations) {
                Resource found = Resources.create(location);
                if (found.exists()) {
                    addSchemas(found);
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
                        addSchemas(Resources.fromClasspath(resource.toString()));
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to initialize Json schema repository", e);
        }
    }

    private void addSchemas(Resource resource) {
        if (resource.getLocation().endsWith(".json")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading json schema resource " + resource.getLocation());
            }
            SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);
            simpleJsonSchema.initialize();
            schemas.add(simpleJsonSchema);
        } else {
            logger.warn("Skipped resource other than json schema for repository (" + resource.getLocation() + ")");
        }
    }

    public String getName() {
        return name;
    }

    public List<SimpleJsonSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<SimpleJsonSchema> schemas) {
        this.schemas = schemas;
    }

    public static Logger getLog() {
        return logger;
    }

    public static void setLog(Logger logger) {
        JsonSchemaRepository.logger = logger;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
