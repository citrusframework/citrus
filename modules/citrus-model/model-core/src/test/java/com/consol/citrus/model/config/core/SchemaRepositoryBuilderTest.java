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
 * @since 2013.04.19
 */
public class SchemaRepositoryBuilderTest {

    @Test
    public void testBuild() throws Exception {
        SchemaRepository schemaRepository = new SchemaRepositoryBuilder()
                .withId("1")
                .addSchema("schema1")
                .addSchema("schema2")
                .build();

        Assert.assertNotNull(schemaRepository);
        Assert.assertEquals(schemaRepository.getId(), "1");
        Assert.assertEquals(schemaRepository.getSchemas().getSchemas().size(), 2);
        Assert.assertEquals(schemaRepository.getSchemas().getSchemas().get(0).getRef(), "schema1");
        Assert.assertEquals(schemaRepository.getSchemas().getSchemas().get(1).getRef(), "schema2");
    }
}
