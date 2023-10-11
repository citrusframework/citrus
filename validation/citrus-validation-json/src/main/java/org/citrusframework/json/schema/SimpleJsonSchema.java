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

package org.citrusframework.json.schema;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;

/**
 * Adapter between the resource reference from the bean configuration and the usable {@link SimpleJsonSchema} for
 * validation.
 */
public class SimpleJsonSchema implements InitializingPhase {

    /** Default json schema factory */
    private final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    /** The Resource of the json schema passed from the bean config */
    private Resource json;

    /** The parsed json schema ready for validation */
    private JsonSchema schema;

    public SimpleJsonSchema(Resource resource) {
        json = resource;
    }

    public SimpleJsonSchema() {
        super();
    }

    @Override
    public void initialize() {
        try (FileInputStream fileInputStream = new FileInputStream(json.getFile())) {
            schema = jsonSchemaFactory.getSchema(fileInputStream);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load Json schema", e);
        }
    }

    public Resource getJson() {
        return json;
    }

    public void setJson(Resource json) {
        this.json = json;
    }

    public JsonSchema getSchema() {
        return schema;
    }

    public void setSchema(JsonSchema schema) {
        this.schema = schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleJsonSchema that = (SimpleJsonSchema) o;
        return Objects.equals(jsonSchemaFactory, that.jsonSchemaFactory) &&
                Objects.equals(json, that.json) &&
                Objects.equals(schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonSchemaFactory, json, schema);
    }
}
