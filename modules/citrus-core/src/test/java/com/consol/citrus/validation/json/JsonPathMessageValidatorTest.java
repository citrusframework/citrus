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

package com.consol.citrus.validation.json;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidatorTest extends AbstractTestNGUnitTest {

    @Autowired
    @Qualifier("defaultJsonPathMessageValidator")
    private JsonPathMessageValidator validator;
    private String payload = "{ \"root\": {"
                    + "\"element\": { \"attributeA\":\"attribute-value\",\"attributeB\":\"attribute-value\",\"sub-element\":\"text-value\" },"
                    + "\"text\": \"text-value\","
                    + "\"number\": 10,"
                    + "\"numbers\": [10, 20, 30, 40],"
                    + "\"person\": {\"name\": \"Penny\"},"
            + "}}";

    private Message message = new DefaultMessage(payload);

    @Test
    public void testValidateMessageElementsWithJsonPathSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$..element.sub-element", "text-value"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$['root']['element']['sub-element']", "text-value"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$..['sub-element']", "text-value"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$..sub-element", "text-value"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$..sub-element", startsWith("text")));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.numbers", "[10, 20, 30, 40]"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.numbers", contains(10L, 20L, 30L, 40L)));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.numbers", hasSize(4)));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.person", "{\"name\":\"Penny\"}"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithJsonPathFunctionsSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$..element.keySet()", "[attributeA, sub-element, attributeB]"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.element.keySet()", Arrays.asList("attributeA", "sub-element", "attributeB")));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.element.keySet()", contains("attributeA", "sub-element", "attributeB")));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$['root']['person'].keySet()", "[name]"));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$['root']['person'].keySet()", hasSize(1)));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.numbers.size()", 4));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.person.size()", 1));
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithValidationMatcherSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("$..element.attributeB", "@endsWith('-value')@");
        validationExpressions.put("$..element.sub-element", "@equalsIgnoreCase('TEXT-VALUE')@");
        validationExpressions.put("$.root.element.sub-element", "@contains('ext-val')@");

        validationContext.setJsonPathExpressions(validationExpressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithJsonPathFunctionsNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.element.keySet()", "[attributeA, attributeB, attributeC]"));
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.element.keySet()", Arrays.asList("attributeA", "attributeB")));
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.numbers.size()", 5));
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap("$.root.person.size()", 0));
        try{
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithValidationMatcherNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("$..element.attributeB", "@endsWith('-value')@");
        validationExpressions.put("$..element.sub-element", "@contains('FAIL')@");

        validationContext.setJsonPathExpressions(validationExpressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithJsonPathNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap(
                "$..element.sub-element", "false-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithFullPathSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap(
                "$.root.element.sub-element", "text-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithFullPathNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setJsonPathExpressions(Collections.<String, Object>singletonMap(
                "$.root.element.sub-element", "false-value"));

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithMixedNotationsSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        //mix of xpath and dot-notation
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.sub-element", "text-value");
        validationExpressions.put("$.root.element.sub-element", "text-value");
        validationContext.setJsonPathExpressions(validationExpressions);

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
}
