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

package org.citrusframework.json.schema;

import java.io.InputStream;
import java.util.Objects;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.resource.IriResourceLoader;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ByteArrayResource;

/**
 * Adapter between the resource reference from the bean configuration and the usable {@link SimpleJsonSchema} for
 * validation.
 */
public class SimpleJsonSchema implements InitializingPhase {

    /** Default json schema factory */
    private final SchemaRegistry jsonSchemaFactory = SchemaRegistry.withDefaultDialect(
        SpecificationVersion.DRAFT_7, builder ->
            // This opts in loading from Iris
            builder.resourceLoaders(resourceLoaders -> resourceLoaders.add(new IriResourceLoader()))
        );

    /** The Resource of the json schema passed from the bean config */
    private Resource json;

    /** The parsed json schema ready for validation */
    private Schema schema;

    public SimpleJsonSchema(Resource resource) {
        json = resource;
    }

    public SimpleJsonSchema() {
        super();
    }

    /**
     * Initializes the JSON schema used by this component.
     *
     * <p>The schema is loaded via a {@link SchemaLocation} rather than directly from an input stream.
     * This enables proper resolution of referenced schemas (e.g. via {@code $ref}) and, in
     * combination with {@code IrisResourceLoader}, also allows loading schemas from HTTP locations.</p>
     */
    @Override
    public void initialize() {

        if (json instanceof ByteArrayResource) {
            initializeFromStream();
        } else {
            try  {
                // All other resources provide a URL, URL allows for loading or dependencies
                schema = jsonSchemaFactory.getSchema(SchemaLocation.of(json.getURL().toString()));
            } catch (Exception e) {
                // If all fails, go for the stream
                initializeFromStream();
            }
        }
    }

    /**
     * Ensure backwards compatibility in any case, loading via SchemaLocation fails.
     */
    private void initializeFromStream() {
        try (InputStream inputStream = json.getInputStream()) {
            schema = jsonSchemaFactory.getSchema(inputStream);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to load Json schema", e);
        }
    }

    public Resource getJson() {
        return json;
    }

    public void setJson(Resource json) {
        this.json = json;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
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
