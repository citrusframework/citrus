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

package org.citrusframework.xml.actions;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.path.DynamicPathExpressionValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Reproduction test for GitHub issue #676.
 * <p>
 * A validate path attribute that references a test variable (e.g. {@code ${VERSION_PATH}}) must not be
 * misinterpreted as JSONPath expression. The expression type is only known at runtime, so the validation
 * context must be resolved dynamically.
 */
public class ReceiveVariablePathValidationTest {

    @Test
    public void shouldCreateDynamicValidationContextForVariablePathExpression() {
        ReceiveMessageAction action = createReceiveAction("${VERSION_PATH}");

        Assert.assertTrue(action.getValidationContexts()
                .stream()
                .anyMatch(DynamicPathExpressionValidationContext.class::isInstance));
        Assert.assertFalse(action.getValidationContexts()
                .stream()
                .anyMatch(JsonPathMessageValidationContext.class::isInstance));
        Assert.assertFalse(action.getValidationContexts()
                .stream()
                .anyMatch(XpathMessageValidationContext.class::isInstance));
    }

    @Test
    public void shouldCreateXpathValidationContextForStaticPathExpression() {
        ReceiveMessageAction action = createReceiveAction("/root/version/@number");

        Assert.assertTrue(action.getValidationContexts()
                .stream()
                .anyMatch(XpathMessageValidationContext.class::isInstance));
        Assert.assertFalse(action.getValidationContexts()
                .stream()
                .anyMatch(JsonPathMessageValidationContext.class::isInstance));
    }

    @Test
    public void shouldCreateJsonPathValidationContextForStaticJsonPathExpression() {
        ReceiveMessageAction action = createReceiveAction("$.version.number");

        Assert.assertTrue(action.getValidationContexts()
                .stream()
                .anyMatch(JsonPathMessageValidationContext.class::isInstance));
        Assert.assertFalse(action.getValidationContexts()
                .stream()
                .anyMatch(XpathMessageValidationContext.class::isInstance));
    }

    private static ReceiveMessageAction createReceiveAction(String path) {
        Receive receive = new Receive();

        Receive.Validate validate = new Receive.Validate();
        validate.setPath(path);
        validate.setValue("1.0");
        receive.getValidates().add(validate);

        return receive.build();
    }
}
