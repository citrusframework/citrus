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

package org.citrusframework.validation.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minidev.json.parser.ParseException;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.report.GraciousProcessingReport;
import org.citrusframework.validation.json.schema.JsonSchemaValidation;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;
import static net.minidev.json.parser.JSONParser.MODE_RFC4627;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class JsonTextMessageValidatorTest extends UnitTestSupport {

    private JsonTextMessageValidator fixture;
    private JsonMessageValidationContext validationContext;

    @BeforeMethod
    public void setup() {
        fixture = new JsonTextMessageValidator();
        validationContext = new JsonMessageValidationContext();
    }

    @Test
    public void testJsonValidationExactMatch() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationPropertyOrdering() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "index": 5
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationIgnoreProperty() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "index": 5
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "@ignore@",
          "text": "Hello World!",
          "index": "@ignore@"
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationPropertyValueMismatch() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Something else!",
          "index": 5
        }
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Values not equal for entry: '$['text']', expected 'Something else!' but was 'Hello World!'");
    }

    @Test
    public void testJsonValidationArrayMatch() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 4, 5]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 4, 5]
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationArrayObjectMatch() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "name": "fluffy", "category": "cat" },
            { "name": "hasso", "category": "dog" }
          ]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "name": "fluffy", "category": "cat" },
            { "name": "hasso", "category": "dog" }
          ]
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationArrayOrdering() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 4, 5]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [5, 4, 3, 2, 1]
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationArrayObjectOrdering() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "name": "fluffy", "category": "cat" },
            { "name": "hasso", "category": "dog" }
          ]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "category": "dog", "name": "hasso" },
            { "name": "fluffy", "category": "cat" }
          ]
        }
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationArrayMismatch() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 4, 5]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 0, 5]
        }
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("An item in '$['numbers']' is missing, expected '0' to be in '[1,2,3,4,5]'");
    }

    @Test
    public void testJsonValidationArraySizeMismatch() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3, 4, 5]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "numbers": [1, 2, 3]
        }
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Number of entries is not equal in element: '$['numbers']', expected '[1,2,3]' but was '[1,2,3,4,5]'");
    }

    @Test
    public void testJsonValidationArrayObjectMismatch() {
        var actualMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "name": "fluffy", "category": "cat" },
            { "name": "hasso", "category": "dog" }
          ]
        }
        """);
        var expectedMessage = new DefaultMessage("""
        {
          "id": "x123456789x",
          "text": "Hello World!",
          "pets": [
            { "name": "fluffy", "category": "cat" },
            { "name": "peppa", "category": "pig" }
          ]
        }
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("An item in '$['pets']' is missing, expected '{\"name\":\"peppa\",\"category\":\"pig\"}' to be in '[{\"name\":\"fluffy\",\"category\":\"cat\"},{\"name\":\"hasso\",\"category\":\"dog\"}]'");
    }

    @Test
    public void testJsonValidationVariableSupport() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello ${world}!\", \"index\":${index}, \"id\":\"${id}\"}");

        context.setVariable("world", "World");
        context.setVariable("index", "5");
        context.setVariable("id", "x123456789x");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testJsonValidationInvalidJsonText() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":invalid, \"id\":\"x123456789x\"}");

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(CitrusRuntimeException.class)
                .cause()
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void testJsonEmptyMessageValidationError() {
        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        var expectedMessage = new DefaultMessage("");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testUseSchemaRepositoryValidatorIfSchemaValidationIsEnabled() {
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext.Builder()
                .schemaValidation(true)
                .build();

        JsonSchemaValidation jsonSchemaValidation = mock(JsonSchemaValidation.class);
        when(jsonSchemaValidation.validate(any(), anyList(), any(), any())).thenReturn(new GraciousProcessingReport((true)));
        fixture.jsonSchemaValidation(jsonSchemaValidation);

        JsonSchemaRepository jsonSchemaRepository = mock(JsonSchemaRepository.class);
        context.getReferenceResolver().bind("jsonSchemaRepository", jsonSchemaRepository);

        var actualMessage = new DefaultMessage("{\"id\":42}");
        var expectedMessage = new DefaultMessage("{\"id\":42}");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));

        verify(jsonSchemaValidation).validate(eq(actualMessage), eq(context), eq(validationContext));
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new ScriptValidationContext(ScriptTypes.GROOVY));
        validationContexts.add(new ScriptValidationContext("something"));
        validationContexts.add(new JsonPathMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isNull();

        validationContexts.add(new XmlMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isInstanceOf(XmlMessageValidationContext.class);

        validationContexts.add(new DefaultMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isInstanceOf(DefaultMessageValidationContext.class);

        validationContexts.add(new JsonMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isInstanceOf(JsonMessageValidationContext.class);
    }

    @Test
    public void testPermissiveModeSimple() {
        fixture.permissiveMode(MODE_JSON_SIMPLE);

        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\",, \"index\":5, \"id\":\"x123456789x\",}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void shouldUseCustomElementValidator() {
        var strict = true;
        var message = new DefaultMessage("{}");

        var elementValidator = spy(new JsonElementValidator(false, context, Set.of()));
        var elementValidatorProvider = mock(JsonElementValidator.Provider.class);

        when(elementValidatorProvider.getValidator(anyBoolean(), any(), any())).thenReturn(elementValidator);

        fixture.strict(strict)
                .elementValidatorProvider(elementValidatorProvider)
                .validateMessage(message, message, context, validationContext);

        verify(elementValidatorProvider).getValidator(strict, context, validationContext);
        verify(elementValidator).validate(any(JsonElementValidatorItem.class));
    }

    @Test
    public void testPermissiveModeStrict() {
        fixture.permissiveMode(MODE_RFC4627);

        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\",, \"index\":5, \"id\":\"x123456789x\",}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(CitrusRuntimeException.class)
                        .hasMessageContaining("Failed to parse JSON text");
    }
}
