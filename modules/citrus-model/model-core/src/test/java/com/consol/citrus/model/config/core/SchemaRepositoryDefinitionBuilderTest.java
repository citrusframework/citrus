/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.model.config.core;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaRepositoryDefinitionBuilderTest {

    @Test
    public void testBuildWithSchemas() throws Exception {
        SchemaRepositoryDefinition schemaRepository = new SchemaRepositoryDefinitionBuilder()
                .withId("schemaRepo")
                .addSchema(new SchemaDefinitionBuilder().withId("schema1").withLocation("location1").build())
                .addSchema("schema2", "location2")
                .build();

        Assert.assertNotNull(schemaRepository);
        Assert.assertEquals(schemaRepository.getId(), "schemaRepo");
        Assert.assertEquals(schemaRepository.getSchemas().getRevesAndSchemas().size(), 2);
        Assert.assertEquals(((SchemaDefinition)schemaRepository.getSchemas().getRevesAndSchemas().get(0)).getId(), "schema1");
        Assert.assertEquals(((SchemaDefinition)schemaRepository.getSchemas().getRevesAndSchemas().get(0)).getLocation(), "location1");
        Assert.assertEquals(((SchemaDefinition)schemaRepository.getSchemas().getRevesAndSchemas().get(1)).getId(), "schema2");
        Assert.assertEquals(((SchemaDefinition)schemaRepository.getSchemas().getRevesAndSchemas().get(1)).getLocation(), "location2");
    }

    @Test
    public void testBuildWithRefs() throws Exception {
        SchemaRepositoryDefinition schemaRepository = new SchemaRepositoryDefinitionBuilder()
                .withId("schemaRepo")
                .addSchemaReference("schema1")
                .addSchemaReference("schema2")
                .build();

        Assert.assertNotNull(schemaRepository);
        Assert.assertEquals(schemaRepository.getId(), "schemaRepo");
        Assert.assertEquals(schemaRepository.getSchemas().getRevesAndSchemas().size(), 2);
        Assert.assertEquals(((SchemaRepositoryDefinition.Schemas.Ref)schemaRepository.getSchemas().getRevesAndSchemas().get(0)).getSchema(), "schema1");
        Assert.assertEquals(((SchemaRepositoryDefinition.Schemas.Ref)schemaRepository.getSchemas().getRevesAndSchemas().get(1)).getSchema(), "schema2");
    }
}
