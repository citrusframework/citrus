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

package com.consol.citrus.validation.xml;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidatorTest extends AbstractTestNGUnitTest {

    @Autowired
    @Qualifier("defaultXpathMessageValidator")
    private XpathMessageValidator validator;

    @Test
    public void testValidateMessageElementsWithXPathSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap("//element/sub-element", "text-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@equalsIgnoreCase('TEXT-VALUE')@");

        validationContext.setXpathExpressions(validationExpressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@contains('FAIL')@");

        validationContext.setXpathExpressions(validationExpressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithXPathNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap(
                "//element/sub-element", "false-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap(
                "root.element.sub-element", "text-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap(
                "root.element.sub-element", "@contains('ext-val')@"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap(
                "root.element.sub-element", "@contains(false-value)@"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.<String, Object>singletonMap(
                "root.element.sub-element", "false-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithMixedNotationsSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        //mix of xpath and dot-notation
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("//element/sub-element", "text-value");
        validationExpressions.put("root.element.sub-element", "text-value");
        validationContext.setXpathExpressions(validationExpressions);

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

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        HashMap<String, Object> expressions = new HashMap<>();
        validationContext.setXpathExpressions(expressions);

        expressions.put("node-set://element/sub-element", "text-value,other-value");
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://element/sub-element", allOf(hasSize(greaterThan(1)), not(empty())));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://element/sub-element", "[text-value, other-value]");
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://element/sub-element", "[text-value,other-value]");
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://@attribute", "[A, B]");
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://@attribute", hasSize(2));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        expressions.put("node-set://@attribute", contains("A", "B"));
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

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        HashMap<String, Object> expressions = new HashMap<>();
        expressions.put("node-set://element/other-element", "");
        expressions.put("boolean://element/other-element", "false");
        expressions.put("boolean://element/sub-element", "true");
        validationContext.setXpathExpressions(expressions);

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

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        HashMap<String, Object> expressions = new HashMap<>();
        expressions.put("number:count(//element/sub-element[.='text-value'])", "2.0");
        expressions.put("integer:count(//element/sub-element[.='text-value'])", "2");
        expressions.put("number:count(//element/sub-element)", greaterThan(1.0));
        expressions.put("integer:count(//element/sub-element)", greaterThan(1));
        validationContext.setXpathExpressions(expressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
}
