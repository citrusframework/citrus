/*
 * Copyright 2006-2024 the original author or authors.
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

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.report.GraciousProcessingReport;
import org.citrusframework.validation.json.schema.JsonSchemaValidation;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class JsonTextMessageValidatorTest extends UnitTestSupport {

    private JsonTextMessageValidator fixture;
    private JsonMessageValidationContext validationContext;

    @BeforeMethod
    public void setup() {
        fixture = new JsonTextMessageValidator();
        validationContext = new JsonMessageValidationContext();
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
        fixture.setJsonSchemaValidation(jsonSchemaValidation);

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
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new ScriptValidationContext(MessageType.JSON.name()));
        validationContexts.add(new ScriptValidationContext(MessageType.XML.name()));
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));
        validationContexts.add(new JsonPathMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isNull();

        validationContexts.add(new JsonMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isNotNull();
    }

    @Test
    public void testPermissiveModeSimple() {
        fixture.setPermissiveMode(JSONParser.MODE_JSON_SIMPLE);

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
        fixture.setPermissiveMode(JSONParser.MODE_RFC4627);

        var actualMessage = new DefaultMessage("{\"text\":\"Hello World!\",, \"index\":5, \"id\":\"x123456789x\",}");
        var expectedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(CitrusRuntimeException.class)
                        .hasMessageContaining("Failed to parse JSON text");
    }
}
