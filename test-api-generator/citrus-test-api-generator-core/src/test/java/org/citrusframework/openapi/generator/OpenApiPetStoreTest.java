package org.citrusframework.openapi.generator;

import static org.citrusframework.openapi.generator.sample.OpenApiPetStore.PetEntityValidationContext.Builder.pet;
import static org.mockito.Mockito.mock;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import java.util.Map;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.DefaultFunctionRegistry;
import org.citrusframework.json.JsonPathUtils;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.AggregateEntityValidationContext;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.EntityValidationContext;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.PetEntityValidationContext;
import org.citrusframework.openapi.generator.sample.OpenApiPetStore.PetEntityValidationContext.Builder;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.DefaultMessageValidatorRegistry;
import org.citrusframework.validation.ValidationUtils;
import org.testng.annotations.Test;

public class OpenApiPetStoreTest {

    @Test
    public void test() {
        Builder petValidationContextBuilder = pet().id("1234")
            .name("Garfield")
            .category("Cat")
            .address(address -> address
                .street("Nina Hagen Hang")
                .zip("12345")
                .city("Hagen ATW"))
//            .owners(anyOf(List.of(
//                owner -> owner.name("Peter Lustig"),
//                owner -> owner.name("Hans Meier")
//            )))
//            .owners(oneOf(List.of(
//                owner -> owner.name("Seppel Hinterhuber")
//            )))
//            .urls(0, "url1")
//            .urls(1, "url2")
//            .urls("@contains('url1', 'url2')")
            ;

        PetEntityValidationContext petValidationContext = petValidationContextBuilder.build();
        OpenApiEntityValidator validator = new OpenApiEntityValidator();

        Message receivedMessage = new DefaultMessage();
        receivedMessage.setPayload("""
            {
                "id": 1234,
                "name": "Garfield",
                "category": "Cat",
                "address": {
                    "street": "Nina Hagen Hang",
                    "zip": "12345",
                    "city": "Hagen ATW"
                },
                "owners": [
                    {
                        "name": "Peter Lustig"
                    },
                    {
                        "name": "Hans Meier"
                    }
                ]
            }
            """);
        TestContext testContext = new TestContext();
        testContext.setReferenceResolver(mock());
        testContext.setMessageValidatorRegistry(new DefaultMessageValidatorRegistry());
        testContext.setFunctionRegistry(new DefaultFunctionRegistry());

        validator.validateMessage(receivedMessage, null, testContext, petValidationContext);


    }

    public class OpenApiEntityValidator extends
        AbstractMessageValidator<EntityValidationContext> {

        public void validateMessage(Message receivedMessage, Message controlMessage,
            TestContext context, EntityValidationContext validationContext) {
            System.out.println("asSD");

            validateJson(receivedMessage.getPayload(String.class), context, validationContext);


        }

        private void validateJson(String jsonString, TestContext context,
            EntityValidationContext validationContext) {
            validateJsonPathExpressions(jsonString, context, validationContext);
            validateNestedJsonPathExpressions(jsonString, context, validationContext);
        }

        @Override
        protected Class<EntityValidationContext> getRequiredValidationContextType() {
            return EntityValidationContext.class;
        }

        @Override
        public boolean supportsMessageType(String messageType, Message message) {
            return true;
        }


        private void validateJsonPathExpressions(String jsonString, TestContext context,
            EntityValidationContext validationContext) {
            String jsonPathExpression;
            try {
                JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
                Object receivedJson = parser.parse(jsonString);
                ReadContext readerContext = JsonPath.parse(receivedJson);

                for (Map.Entry<String, Object> entry : validationContext.getJsonPathExpressions()
                    .entrySet()) {
                    Object expectedValue = entry.getValue();

                    jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                    Object jsonPathResult = JsonPathUtils.evaluate(readerContext,
                        jsonPathExpression);

                    if (expectedValue instanceof EntityValidationContext entityValidationContext) {
                        validateJson((String) jsonPathResult, context, entityValidationContext);
                    } else if (expectedValue instanceof AggregateEntityValidationContext<?>) {

                    } else {

                        if (expectedValue instanceof String) {
                            //check if expected value is variable or function (and resolve it, if yes)
                            expectedValue = context.replaceDynamicContentInString(
                                String.valueOf(expectedValue));
                        }

                        //do the validation of actual and expected value for element
                        ValidationUtils.validateValues(jsonPathResult, expectedValue,
                            jsonPathExpression, context);

                        logger.debug("Validating element: {}='{}': OK", jsonPathExpression,
                            expectedValue);
                    }

                }

                logger.debug("JSONPath element validation successful: All values OK");
            } catch (ParseException e) {
                throw new CitrusRuntimeException("Failed to parse JSON text", e);
            }
        }

        private void validateNestedJsonPathExpressions(String jsonString, TestContext context,
            EntityValidationContext validationContext) {
            String jsonPathExpression;
            try {
                JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
                Object receivedJson = parser.parse(jsonString);
                ReadContext readerContext = JsonPath.parse(receivedJson);

                for (Map.Entry<String, EntityValidationContext> entry : validationContext.getNestedValidationContextsBuilders()
                    .entrySet()) {

                    jsonPathExpression = context.replaceDynamicContentInString(entry.getKey());
                    Object jsonPathResult = JsonPathUtils.evaluate(readerContext,
                        jsonPathExpression);

                    validateJson(jsonPathResult.toString(), context, entry.getValue());

                }

                logger.debug("JSONPath element validation successful: All values OK");
            } catch (ParseException e) {
                throw new CitrusRuntimeException("Failed to parse JSON text", e);
            }
        }
    }
}
