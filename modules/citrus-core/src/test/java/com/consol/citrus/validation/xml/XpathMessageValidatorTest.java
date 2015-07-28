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
        validationContext.setXpathExpressions(Collections.singletonMap("//element/sub-element", "text-value"));

        validator.validateMessage(message, context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, String> validationExpressions = new HashMap<String, String>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@equalsIgnoreCase('TEXT-VALUE')@");

        validationContext.setXpathExpressions(validationExpressions);

        validator.validateMessage(message, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, String> validationExpressions = new HashMap<String, String>();
        validationExpressions.put("//element/@attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("//element/@attributeB", "@endsWith('-value')@");
        validationExpressions.put("//element/sub-element", "@contains('FAIL')@");

        validationContext.setXpathExpressions(validationExpressions);

        validator.validateMessage(message, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithXPathNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.singletonMap(
                "//element/sub-element", "false-value"));

        validator.validateMessage(message, context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.singletonMap(
                "root.element.sub-element", "text-value"));

        validator.validateMessage(message, context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithDotNotationValidationMatcherSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.singletonMap(
                "root.element.sub-element", "@contains('ext-val')@"));

        validator.validateMessage(message, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationValidationMatcherNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.singletonMap(
                "root.element.sub-element", "@contains(false-value)@"));

        validator.validateMessage(message, context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithDotNotationNotSuccessful() {
        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                + "<sub-element attribute='A'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setXpathExpressions(Collections.singletonMap(
                "root.element.sub-element", "false-value"));

        validator.validateMessage(message, context, validationContext);
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
        Map<String, String> validationExpressions = new HashMap<String, String>();
        validationExpressions.put("//element/sub-element", "text-value");
        validationExpressions.put("root.element.sub-element", "text-value");
        validationContext.setXpathExpressions(validationExpressions);

        validator.validateMessage(message, context, validationContext);
    }
}
