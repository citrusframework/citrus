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

package org.citrusframework.spring.config.xml;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.path.DynamicPathExpressionValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression test for GitHub issue #676.
 * A path expression that references a test variable (e.g. ${VERSION_PATH}) must not be
 * misinterpreted as a JSONPath expression just because it starts with a dollar sign. The
 * type of the path expression can only be determined at runtime when the variable gets
 * resolved. So the parser must use a {@link DynamicPathExpressionValidationContext} instead.
 */
public class ReceiveMessageActionParserVariablePathTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParserWithVariablePath() {
        assertActionCount(3);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");

        ReceiveMessageAction action = getNextTestActionFromTest();
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof DynamicPathExpressionValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);

        DynamicPathExpressionValidationContext dynamicValidationContext =
                (DynamicPathExpressionValidationContext) action.getValidationContexts().get(0);
        Assertions.assertEquals(1, dynamicValidationContext.getExpressions().size());
        Assertions.assertEquals("1.0", dynamicValidationContext.getExpressions().get("${VERSION_PATH}"));
        Assertions.assertTrue(dynamicValidationContext.requiresValidator());

        action = getNextTestActionFromTest();
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof DynamicPathExpressionValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);

        dynamicValidationContext = (DynamicPathExpressionValidationContext) action.getValidationContexts().get(0);
        Assertions.assertEquals(1, dynamicValidationContext.getExpressions().size());
        Assertions.assertEquals("1.0", dynamicValidationContext.getExpressions().get("${JSON_PATH}"));

        action = getNextTestActionFromTest();
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assertions.assertFalse(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
    }
}
