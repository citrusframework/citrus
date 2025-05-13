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

package org.citrusframework.validation.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.scanner.ScannerException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YamlMessageValidatorTest extends UnitTestSupport {

    private YamlMessageValidator fixture;
    private YamlMessageValidationContext validationContext;

    @BeforeMethod
    public void setup() {
        fixture = new YamlMessageValidator();
        validationContext = new YamlMessageValidationContext();
    }

    @Test
    public void testYamlValidationExactMatch() {
        var actualMessage = new DefaultMessage("""
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        """);
        var expectedMessage = new DefaultMessage("""
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationArrayExactMatch() {
        var actualMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        - text: "Good Bye!"
          index: 1
          id: "x987654321x"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        - text: "Good Bye!"
          index: 1
          id: "x987654321x"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationPropertyMismatch() {
        var actualMessage = new DefaultMessage("""
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        """);
        var expectedMessage = new DefaultMessage("""
        text: "Something else!"
        index: 5
        id: "x123456789x"
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Values not equal for entry: '$.text', expected 'Something else!' but was 'Hello World!'");
    }

    @Test
    public void testYamlValidationArrayMismatch() {
        var actualMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        - text: "Good Bye!"
          index: 1
          id: "x987654321x"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        - text: "Something else!"
          index: 1
          id: "x987654321x"
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("An item in '$' is missing, expected '{text=Something else!, index=1, id=x987654321x}' " +
                        "to be in '[{text=Hello World!, index=5, id=x123456789x}, {text=Good Bye!, index=1, id=x987654321x}]'");
    }

    @Test
    public void testYamlValidationMultipleDocuments() {
        var actualMessage = new DefaultMessage("""
        ---
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        ---
        text: "Good Bye!"
        index: 1
        id: "x987654321x"
        """);
        var expectedMessage = new DefaultMessage("""
        ---
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        ---
        text: "Good Bye!"
        index: 1
        id: "x987654321x"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationMultipleDocumentsMismatch() {
        var actualMessage = new DefaultMessage("""
        ---
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        ---
        text: "Good Bye!"
        index: 1
        id: "x987654321x"
        """);
        var expectedMessage = new DefaultMessage("""
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed - number of YAML documents not equal, expected '1' but was '2'");
    }

    @Test
    public void testYamlValidationMultipleDocumentsPropertyMismatch() {
        var actualMessage = new DefaultMessage("""
        ---
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        ---
        text: "Good Bye!"
        index: 1
        id: "x987654321x"
        """);
        var expectedMessage = new DefaultMessage("""
        ---
        text: "Hello World!"
        index: 5
        id: "x123456789x"
        ---
        text: "Something else!"
        index: 1
        id: "x987654321x"
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Values not equal for entry: '$.text', expected 'Something else!' but was 'Good Bye!'");
    }

    @Test
    public void testYamlValidationRandomPropertyOrder() {
        var actualMessage = new DefaultMessage("""
        - id: "x123456789x"
          index: 5
          text: "Hello World!"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationIgnoreElement() {
        var actualMessage = new DefaultMessage("""
        - id: "x123456789x"
          index: 5
          text: "Hello World!"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: "@ignore@"
          id: "@ignore@"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationIgnoreSubtree() {
        var actualMessage = new DefaultMessage("""
        - id: "x123456789x"
          index: 5
          text: "Hello World!"
          nested:
            message: "Good Bye"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: "@ignore@"
          id: "@ignore@"
          nested: "@ignore@"
        """);

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationNonStrict() {
        var actualMessage = new DefaultMessage("""
        - id: "x123456789x"
          index: 5
          text: "Hello World!"
          additional:
            message: "Good Bye"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: "@ignore@"
          id: "@ignore@"
        """);

        try {
            fixture.strict(false);
            assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
        } finally {
            fixture.strict(true);
        }
    }

    @Test
    public void testYamlValidationVariableSupport() {
        var actualMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello ${world}!"
          index: ${index}
          id: "${id}"
        """);

        context.setVariable("world", "World");
        context.setVariable("index", "5");
        context.setVariable("id", "x123456789x");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void testYamlValidationInvalidSource() {
        var actualMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        """);
        var expectedMessage = new DefaultMessage("""
        - text: "Hello World!"
          invalid
          id: "x123456789x"
        """);

        assertThatThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext))
                .isInstanceOf(CitrusRuntimeException.class)
                .cause()
                .isInstanceOf(ScannerException.class);
    }

    @Test
    public void testYamlEmptyMessageValidationError() {
        var actualMessage = new DefaultMessage("""
        - text: "Hello World!"
          index: 5
          id: "x123456789x"
        """);
        var expectedMessage = new DefaultMessage("");

        assertThatNoException().isThrownBy(() -> fixture.validateMessage(actualMessage, expectedMessage, context, validationContext));
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new ScriptValidationContext(ScriptTypes.GROOVY));
        validationContexts.add(new ScriptValidationContext("something"));

        assertThat(fixture.findValidationContext(validationContexts)).isNull();

        validationContexts.add(new DefaultMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isInstanceOf(DefaultMessageValidationContext.class);

        validationContexts.add(new YamlMessageValidationContext());

        assertThat(fixture.findValidationContext(validationContexts)).isInstanceOf(YamlMessageValidationContext.class);
    }

    @Test
    public void testLookup() {
        Map<String, MessageValidator<? extends ValidationContext>> validators = MessageValidator.lookup();
        Assert.assertEquals(validators.size(), 2L);
        Assert.assertNotNull(validators.get("defaultMessageHeaderValidator"));
        Assert.assertEquals(validators.get("defaultMessageHeaderValidator").getClass(), DefaultMessageHeaderValidator.class);
        Assert.assertNotNull(validators.get("defaultYamlMessageValidator"));
        Assert.assertEquals(validators.get("defaultYamlMessageValidator").getClass(), YamlMessageValidator.class);
    }

    @Test
    public void testTestLookup() {
        Assert.assertTrue(MessageValidator.lookup("header").isPresent());
        Assert.assertTrue(MessageValidator.lookup("yaml").isPresent());
    }
}
