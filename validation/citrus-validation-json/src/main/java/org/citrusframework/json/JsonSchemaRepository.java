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

package org.citrusframework.json;

import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.repository.BaseRepository;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Schema repository holding a set of json schema resources known in the test scope.
 * @since 2.7.3
 */
public class JsonSchemaRepository extends BaseRepository {

    /** Logger */
    private static Logger logger = LoggerFactory.getLogger(JsonSchemaRepository.class);

    private static final String DEFAULT_NAME = "jsonSchemaRepository";

    /** List of schema resources */
    private List<SimpleJsonSchema> schemas = new ArrayList<>();

    public JsonSchemaRepository() {
        super(DEFAULT_NAME);
    }

    @Override
    protected void addRepository(Resource resource) {
        if (resource.getLocation().endsWith(".json")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading json schema resource '{}'", resource.getLocation());
            }

            SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);
            simpleJsonSchema.initialize();
            schemas.add(simpleJsonSchema);
        } else {
            logger.warn("Skipped resource other than json schema for repository '{}'", resource.getLocation());
        }
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

    public void addSchema(SimpleJsonSchema simpleJsonSchema) {
        schemas.add(simpleJsonSchema);
    }
}
