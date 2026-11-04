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

package org.citrusframework.openapi.model.v2;

import java.util.List;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasResponse;
import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v2.models.Oas20Document;
import io.apicurio.datamodels.openapi.v2.models.Oas20Items;
import io.apicurio.datamodels.openapi.v2.models.Oas20Operation;
import io.apicurio.datamodels.openapi.v2.models.Oas20Parameter;
import io.apicurio.datamodels.openapi.v2.models.Oas20Response;
import io.apicurio.datamodels.openapi.v2.models.Oas20Responses;
import io.apicurio.datamodels.openapi.v2.models.Oas20Schema;
import org.citrusframework.openapi.model.OasModelHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Oas20ModelHelperTest {

    @Test
    public void shouldFindRandomResponseWithGoodStatusCode() {
        Oas20Document document = new Oas20Document();
        Oas20Operation operation = new Oas20Operation("GET");

        operation.responses = new Oas20Responses();

        Oas20Response nokResponse = new Oas20Response("403");
        nokResponse.schema = new Oas20Schema();

        Oas20Response okResponse = new Oas20Response("200");
        okResponse.schema = new Oas20Schema();

        operation.responses = new Oas20Responses();
        operation.responses.addResponse("403", nokResponse);
        operation.responses.addResponse("200", okResponse);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
                document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals(okResponse, responseForRandomGeneration.get());
    }

    @Test
    public void shouldFindFirstResponseInAbsenceOfAGoodOne() {
        Oas20Document document = new Oas20Document();
        Oas20Operation operation = new Oas20Operation("GET");

        operation.responses = new Oas20Responses();

        Oas20Response nokResponse403 = new Oas20Response("403");
        nokResponse403.schema = new Oas20Schema();
        Oas20Response nokResponse407 = new Oas20Response("407");
        nokResponse407.schema = new Oas20Schema();

        operation.responses = new Oas20Responses();
        operation.responses.addResponse("403", nokResponse403);
        operation.responses.addResponse("407", nokResponse407);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
                document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals("403", responseForRandomGeneration.get().getStatusCode());
    }

    @Test
    public void shouldFindDefaultResponseInAbsenceOfAGoodOne() {
        Oas20Document document = new Oas20Document();
        Oas20Operation operation = new Oas20Operation("GET");

        operation.responses = new Oas20Responses();

        Oas20Response nokResponse403 = new Oas20Response("403");
        nokResponse403.schema = new Oas20Schema();
        Oas20Response nokResponse407 = new Oas20Response("407");
        nokResponse407.schema = new Oas20Schema();

        operation.responses = new Oas20Responses();
        operation.responses.default_ = nokResponse407;
        operation.responses.addResponse("403", nokResponse403);
        operation.responses.addResponse("407", nokResponse407);

        Optional<OasResponse> responseForRandomGeneration = OasModelHelper.getResponseForRandomGeneration(
                document, operation, null, null);
        assertTrue(responseForRandomGeneration.isPresent());
        assertEquals("407", responseForRandomGeneration.get().getStatusCode());
    }

    @Test
    public void shouldFindParameterSchema() {
        Oas20Parameter parameter = new Oas20Parameter();
        parameter.schema = new Oas20Schema();

        Optional<OasSchema> parameterSchema = Oas20ModelHelper.getParameterSchema(parameter);
        assertTrue(parameterSchema.isPresent());
        assertEquals(parameter.schema, parameterSchema.get());
    }

    @Test
    public void shouldFindSchemaFromParameter() {
        Oas20Parameter parameter = new Oas20Parameter("testParameter");
        parameter.type = "string";
        parameter.format = "date-time";
        parameter.items = new Oas20Items();
        parameter.multipleOf = 2;
        parameter.default_ = "defaultValue";
        parameter.enum_ = List.of("value1", "value2");
        parameter.pattern = "pattern";
        parameter.description = "description";
        parameter.uniqueItems = true;
        parameter.maximum = 100.0;
        parameter.maxItems = 10;
        parameter.maxLength = 20;
        parameter.exclusiveMaximum = true;
        parameter.minimum = 0.0;
        parameter.minItems = 1;
        parameter.minLength = 5;
        parameter.exclusiveMinimum = false;

        Optional<OasSchema> schemaOptional = Oas20ModelHelper.getParameterSchema(parameter);
        assertTrue(schemaOptional.isPresent());

        OasSchema parameterSchema = schemaOptional.get();
        assertEquals("testParameter", parameterSchema.title);
        assertEquals("string", parameterSchema.type);
        assertEquals("date-time", parameterSchema.format);
        assertEquals(parameterSchema.items, parameter.items);
        assertEquals(parameterSchema.multipleOf, parameter.multipleOf);
        assertEquals(parameterSchema.default_, parameter.default_);
        assertEquals(parameterSchema.enum_, parameter.enum_);
        assertEquals(parameterSchema.pattern, parameter.pattern);
        assertEquals(parameterSchema.description, parameter.description);
        assertEquals(parameterSchema.uniqueItems, parameter.uniqueItems);
        assertEquals(parameterSchema.maximum, parameter.maximum);
        assertEquals(parameterSchema.maxItems, parameter.maxItems);
        assertEquals(parameterSchema.maxLength, parameter.maxLength);
        assertEquals(parameterSchema.exclusiveMaximum, parameter.exclusiveMaximum);
        assertEquals(parameterSchema.minimum, parameter.minimum);
        assertEquals(parameterSchema.minItems, parameter.minItems);
        assertEquals(parameterSchema.minLength, parameter.minLength);
        assertEquals(parameterSchema.exclusiveMinimum, parameter.exclusiveMinimum);
    }
}
