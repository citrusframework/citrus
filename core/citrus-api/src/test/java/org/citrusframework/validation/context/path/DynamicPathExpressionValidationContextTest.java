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

package org.citrusframework.validation.context.path;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that dynamic path expressions are resolved at runtime and afterwards
 * classified as JSONPath or XPath expression.
 */
public class DynamicPathExpressionValidationContextTest {

    @Test
    public void shouldResolveVariableToXpathExpression() {
        TestContext context = new TestContext();
        context.setVariable("VERSION_PATH", "/root/version/@number");

        DynamicPathExpressionValidationContext validationContext = new DynamicPathExpressionValidationContext.Builder()
                .expression("${VERSION_PATH}", "1.0")
                .build();

        Assertions.assertTrue(validationContext.requiresValidator());
        Assertions.assertTrue(validationContext.getCorrespondingValidationModule().isEmpty());

        List<ValidationContext> resolvedContexts = validationContext.resolve(context);

        Assertions.assertEquals(1, resolvedContexts.size());
        Assertions.assertTrue(resolvedContexts.get(0) instanceof XpathMessageValidationContext);

        XpathMessageValidationContext xpathValidationContext = (XpathMessageValidationContext) resolvedContexts.get(0);
        Assertions.assertEquals(1, xpathValidationContext.getXpathExpressions().size());
        Assertions.assertEquals("1.0", xpathValidationContext.getXpathExpressions().get("/root/version/@number"));
    }

    @Test
    public void shouldResolveVariableToJsonPathExpression() {
        TestContext context = new TestContext();
        context.setVariable("VERSION_PATH", "$.version.number");

        DynamicPathExpressionValidationContext validationContext = new DynamicPathExpressionValidationContext.Builder()
                .expression("${VERSION_PATH}", "1.0")
                .build();

        List<ValidationContext> resolvedContexts = validationContext.resolve(context);

        Assertions.assertEquals(2, resolvedContexts.size());
        Assertions.assertTrue(resolvedContexts.get(0) instanceof JsonMessageValidationContext);
        Assertions.assertTrue(resolvedContexts.get(1) instanceof JsonPathMessageValidationContext);

        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext) resolvedContexts.get(1);
        Assertions.assertEquals(1, jsonPathValidationContext.getJsonPathExpressions().size());
        Assertions.assertEquals("1.0", jsonPathValidationContext.getJsonPathExpressions().get("$.version.number"));
    }

    @Test
    public void shouldResolveMixedPathExpressions() {
        TestContext context = new TestContext();
        context.setVariable("XPATH_PATH", "/root/version/@number");
        context.setVariable("JSON_PATH", "$.version.number");

        DynamicPathExpressionValidationContext validationContext = new DynamicPathExpressionValidationContext.Builder()
                .expression("${XPATH_PATH}", "1.0")
                .expression("${JSON_PATH}", "1.0")
                .build();

        List<ValidationContext> resolvedContexts = validationContext.resolve(context);

        Assertions.assertEquals(3, resolvedContexts.size());
        Assertions.assertTrue(resolvedContexts.stream().anyMatch(XpathMessageValidationContext.class::isInstance));
        Assertions.assertTrue(resolvedContexts.stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assertions.assertTrue(resolvedContexts.stream().anyMatch(JsonPathMessageValidationContext.class::isInstance));
    }

    @Test
    public void shouldResolveResultTypePrefixedXpathExpression() {
        TestContext context = new TestContext();
        context.setVariable("VERSION_PATH", "/root/version/@number");

        DynamicPathExpressionValidationContext validationContext = new DynamicPathExpressionValidationContext.Builder()
                .expression("boolean:${VERSION_PATH}", "true")
                .build();

        List<ValidationContext> resolvedContexts = validationContext.resolve(context);

        Assertions.assertEquals(1, resolvedContexts.size());
        Assertions.assertTrue(resolvedContexts.get(0) instanceof XpathMessageValidationContext);

        XpathMessageValidationContext xpathValidationContext = (XpathMessageValidationContext) resolvedContexts.get(0);
        Assertions.assertEquals("true", xpathValidationContext.getXpathExpressions().get("boolean:/root/version/@number"));
    }

    @Test
    public void shouldFailOnUnknownVariable() {
        TestContext context = new TestContext();

        DynamicPathExpressionValidationContext validationContext = new DynamicPathExpressionValidationContext.Builder()
                .expression("${UNKNOWN}", "1.0")
                .build();

        assertThrows(CitrusRuntimeException.class, () -> validationContext.resolve(context));
    }
}
