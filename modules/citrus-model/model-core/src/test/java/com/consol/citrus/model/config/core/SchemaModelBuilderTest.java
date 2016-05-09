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

public class SchemaModelBuilderTest {
    @Test
    public void testBuild() throws Exception {
        SchemaModel schema = new SchemaModelBuilder()
                .withId("schema1")
                .withLocation("location1")
                .build();

        Assert.assertNotNull(schema);
        Assert.assertEquals(schema.getId(), "schema1");
        Assert.assertEquals(schema.getLocation(), "location1");
    }

}