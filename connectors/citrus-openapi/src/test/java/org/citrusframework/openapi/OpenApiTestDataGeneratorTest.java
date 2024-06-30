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

package org.citrusframework.openapi;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema.Oas20AllOfSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import java.util.HashMap;
import java.util.List;
import org.testng.annotations.Test;

public class OpenApiTestDataGeneratorTest {

    @Test
    public void anyOfIsIgnoredForOas3() {

        Oas30Schema anyOfSchema = new Oas30Schema();
        anyOfSchema.anyOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(OpenApiTestDataGenerator.createValidationExpression(
            anyOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void allOfIsIgnoredForOas3() {

        Oas30Schema allOfSchema = new Oas30Schema();
        allOfSchema.allOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(OpenApiTestDataGenerator.createValidationExpression(
            allOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void oneOfIsIgnoredForOas3() {

        Oas30Schema oneOfSchema = new Oas30Schema();
        oneOfSchema.oneOf = List.of(new Oas30Schema(), new Oas30Schema());

        assertEquals(OpenApiTestDataGenerator.createValidationExpression(
            oneOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }

    @Test
    public void allOfIsIgnoredForOas2() {

        Oas20AllOfSchema allOfSchema = new Oas20AllOfSchema();
        allOfSchema.allOf = List.of(new Oas20Schema(), new Oas20Schema());

        assertEquals(OpenApiTestDataGenerator.createValidationExpression(
            allOfSchema, new HashMap<>(), true, mock()), "\"@ignore@\"");
    }
}
