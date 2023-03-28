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
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

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
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        for (String location : schemas) {
            try {
                Resource[] findings = resourcePatternResolver.getResources(location);

                for (Resource finding : findings) {
                    if (finding.getFilename().endsWith(".xsd") || finding.getFilename().endsWith(".wsdl")) {
                        schemaResources.add(finding);
                    }
                }
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read schema resources for location: " + location, e);
            }
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
