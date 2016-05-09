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
public class SchemaRepositoryModelBuilderTest {

    @Test
    public void testBuildWithSchemas() throws Exception {
        SchemaRepositoryModel schemaRepository = new SchemaRepositoryModelBuilder()
                .withId("schemaRepo")
                .addSchema(new SchemaModelBuilder().withId("schema1").withLocation("location1").build())
                .addSchema("schema2", "location2")
                .build();

        Assert.assertNotNull(schemaRepository);
        Assert.assertEquals(schemaRepository.getId(), "schemaRepo");
        Assert.assertEquals(schemaRepository.getSchemas().getReferences().size(), 0);
        Assert.assertEquals(schemaRepository.getSchemas().getSchemas().size(), 2);
        Assert.assertEquals((schemaRepository.getSchemas().getSchemas().get(0)).getId(), "schema1");
        Assert.assertEquals((schemaRepository.getSchemas().getSchemas().get(0)).getLocation(), "location1");
        Assert.assertEquals((schemaRepository.getSchemas().getSchemas().get(1)).getId(), "schema2");
        Assert.assertEquals((schemaRepository.getSchemas().getSchemas().get(1)).getLocation(), "location2");
    }

    @Test
    public void testBuildWithRefs() throws Exception {
        SchemaRepositoryModel schemaRepository = new SchemaRepositoryModelBuilder()
                .withId("schemaRepo")
                .addSchemaReference("schema1")
                .addSchemaReference("schema2")
                .build();

        Assert.assertNotNull(schemaRepository);
        Assert.assertEquals(schemaRepository.getId(), "schemaRepo");
        Assert.assertEquals(schemaRepository.getSchemas().getSchemas().size(), 0);
        Assert.assertEquals(schemaRepository.getSchemas().getReferences().size(), 2);
        Assert.assertEquals((schemaRepository.getSchemas().getReferences().get(0)).getSchema(), "schema1");
        Assert.assertEquals((schemaRepository.getSchemas().getReferences().get(1)).getSchema(), "schema2");
    }
}
