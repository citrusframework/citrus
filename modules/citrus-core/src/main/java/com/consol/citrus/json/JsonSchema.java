/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.json;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

public class JsonSchema implements InitializingBean {


    /** */
    private JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.byDefault();

    private Resource json;
    private com.github.fge.jsonschema.main.JsonSchema schema;

    JsonSchema(Resource resource) {
        json = resource;
    }

    public JsonSchema(){ }

    @Override
    public void afterPropertiesSet() throws Exception {
        schema = jsonSchemaFactory.getJsonSchema(JsonLoader.fromFile(json.getFile()));
    }

    public Resource getJson() {
        return json;
    }

    public void setJson(Resource json) {
        this.json = json;
    }

    public com.github.fge.jsonschema.main.JsonSchema getSchema() {
        return schema;
    }

    public void setSchema(com.github.fge.jsonschema.main.JsonSchema schema) {
        this.schema = schema;
    }
}
