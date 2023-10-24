/*
 * Copyright 2006-2012 the original author or authors.
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
package org.citrusframework.xml.schema;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;

/**
 * Schema combines multiple file resources usually with exactly the same target namespace to
 * one single schema instance.
 *
 * @author Christoph Deppisch
 */
public class XsdSchemaCollection extends AbstractSchemaCollection {

    /** List of schema locations loaded as schema resource instance */
    protected List<String> schemas = new ArrayList<>();

    /**
     * Loads all schema resource files from schema locations.
     */
    protected Resource loadSchemaResources() {
        try {
            ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
            for (String location : schemas) {
                Resource found = Resources.create(location);
                if (found.exists()) {
                    schemaResources.add(found);
                } else {
                    Set<Path> findings;
                    if (StringUtils.hasText(FileUtils.getFileExtension(location))) {
                        String fileNamePattern = FileUtils.getFileName(location).replace(".", "\\.").replace("*", ".*");
                        String basePath = FileUtils.getBasePath(location);
                        findings = resourceResolver.getResources(basePath, fileNamePattern);
                    } else {
                        findings = resourceResolver.getResources(location);
                    }

                    for (Path finding : findings) {
                        if (finding.toString().endsWith(".xsd") || finding.toString().endsWith(".wsdl")) {
                            schemaResources.add(Resources.fromClasspath(finding.toString()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read schema collection resources", e);
        }

        return schemaResources.get(0);
    }

    /**
     * Gets the schemas included in this collection.
     * @return
     */
    public List<String> getSchemas() {
        return schemas;
    }

    /**
     * Sets the schemas in this collection.
     * @param schemas the schema resources to set
     */
    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }
}
