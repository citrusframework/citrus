package org.citrusframework.openapi.model.v2;

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
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
        assertEquals(responseForRandomGeneration.get().getStatusCode(), "403");
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
        assertEquals(responseForRandomGeneration.get().getStatusCode(), "407");
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
        assertEquals(parameterSchema.title, "testParameter");
        assertEquals(parameterSchema.type, "string");
        assertEquals(parameterSchema.format, "date-time");
        assertEquals(parameter.items, parameterSchema.items);
        assertEquals(parameter.multipleOf, parameterSchema.multipleOf);
        assertEquals(parameter.default_, parameterSchema.default_);
        assertEquals(parameter.enum_, parameterSchema.enum_);
        assertEquals(parameter.pattern, parameterSchema.pattern);
        assertEquals(parameter.description, parameterSchema.description);
        assertEquals(parameter.uniqueItems, parameterSchema.uniqueItems);
        assertEquals(parameter.maximum, parameterSchema.maximum);
        assertEquals(parameter.maxItems, parameterSchema.maxItems);
        assertEquals(parameter.maxLength, parameterSchema.maxLength);
        assertEquals(parameter.exclusiveMaximum, parameterSchema.exclusiveMaximum);
        assertEquals(parameter.minimum, parameterSchema.minimum);
        assertEquals(parameter.minItems, parameterSchema.minItems);
        assertEquals(parameter.minLength, parameterSchema.minLength);
        assertEquals(parameter.exclusiveMinimum, parameterSchema.exclusiveMinimum);

    }

}
