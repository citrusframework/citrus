/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.validation.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidatorTest extends UnitTestSupport {

    private XpathMessageValidator validator = new XpathMessageValidator();

    @Test
    public void testValidateMessageElementsWithXPathSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("//element/sub-element", "text-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@equalsIgnoreCase('TEXT-VALUE')@");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@contains('FAIL')@");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithXPathNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("//element/sub-element", "false-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("root.element.sub-element", "text-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("root.element.sub-element", "@contains('ext-val')@")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("root.element.sub-element", "@contains(false-value)@")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("root.element.sub-element", "false-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithMixedNotationsSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        //mix of xpath and dot-notation
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/sub-element", "text-value");
        validationExpressions.put("root.element.sub-element", "text-value");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithNodeListResult() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "<sub-element attribute='B'>other-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://element/sub-element", "text-value,other-value")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://element/sub-element", allOf(hasSize(greaterThan(1)), not(empty())))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://element/sub-element", "[text-value, other-value]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://element/sub-element", "[text-value,other-value]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://@attribute", "[A, B]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://@attribute", hasSize(2))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new XpathMessageValidationContext.Builder()
                .expression("node-set://@attribute", contains("A", "B"))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithNodeListResultNoMatch() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "<sub-element attribute='B'>other-value</sub-element>"
                + "</element>"
                + "</root>");

        HashMap<String, Object> expressions = new HashMap<>();
        expressions.put("node-set://element/other-element", "");
        expressions.put("boolean://element/other-element", "false");
        expressions.put("boolean://element/sub-element", "true");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(expressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithNodeListCount() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "<sub-element attribute='B'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        HashMap<String, Object> expressions = new HashMap<>();
        expressions.put("number:count(//element/sub-element[.='text-value'])", "2.0");
        expressions.put("integer:count(//element/sub-element[.='text-value'])", "2");
        expressions.put("number:count(//element/sub-element)", greaterThan(1.0));
        expressions.put("integer:count(//element/sub-element)", greaterThan(1));

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .expressions(expressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));

        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new XpathMessageValidationContext());

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }
}
