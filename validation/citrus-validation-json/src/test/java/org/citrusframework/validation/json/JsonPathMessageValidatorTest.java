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

package org.citrusframework.validation.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidatorTest extends UnitTestSupport {

    private final JsonPathMessageValidator validator = new JsonPathMessageValidator();
    private final String payload = "{ \"root\": {"
                    + "\"element\": { \"attributeA\":\"attribute-value\",\"attributeB\":\"attribute-value\",\"sub-element\":\"text-value\" },"
                    + "\"text\": \"text-value\","
                    + "\"nullValue\": null,"
                    + "\"number\": 10,"
                    + "\"numbers\": [10, 20, 30, 40],"
                    + "\"person\": {\"name\": \"Penny\"},"
                    + "\"nerds\": [ {\"name\": \"Leonard\"}, {\"name\": \"Sheldon\"} ]"
            + "}}";

    private final Message message = new DefaultMessage(payload);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getValidationMatcherRegistry().getLibraryForPrefix("").getMembers().put("assertThat", new NullValueMatcher());
        return factory;
    }

    @Test
    public void testValidateMessageElementsWithJsonPathSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..element.sub-element", "text-value")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$['root']['element']['sub-element']", "text-value")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..['sub-element']", "text-value")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..sub-element", "text-value")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..sub-element", startsWith("text"))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..name", hasItem("Penny"))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..name", containsInAnyOrder("Penny", "Leonard", "Sheldon"))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.nerds", hasSize(2))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.numbers", "[10, 20, 30, 40]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.numbers", contains(10L, 20L, 30L, 40L))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.numbers", hasSize(4))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.person", "{\"name\":\"Penny\"}")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithJsonPathFunctionsSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..element.keySet()", "[attributeA, sub-element, attributeB]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.element.keySet()", Arrays.asList("attributeA", "sub-element", "attributeB"))
                        .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.element.keySet()", contains("attributeA", "sub-element", "attributeB"))
                        .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$['root']['person'].keySet()", "[name]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$['root']['person'].keySet()", hasSize(1))
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.numbers.size()", 4)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.person.size()", 1)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.person.exists()", true)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.foo.exists()", false)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.nullValue", "")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.nerds.size()", 2L)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.nerds.toString()", "[{\"name\":\"Leonard\"},{\"name\":\"Sheldon\"}]")
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..sub-element.size()", 1L)
                .build();
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithValidationMatcherSuccessful() {
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("$..element.attributeB", "@endsWith('-value')@");
        validationExpressions.put("$..element.sub-element", "@equalsIgnoreCase('TEXT-VALUE')@");
        validationExpressions.put("$.root.element.sub-element", "@contains('ext-val')@");
        validationExpressions.put("$.root.nullValue", "@assertThat(nullValue())@");

        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithJsonPathFunctionsNotSuccessful() {
        JsonPathMessageValidationContext validationContext = validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.element.keySet()", "[attributeA, attributeB, attributeC]")
                .build();
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.element.keySet()", Arrays.asList("attributeA", "attributeB"))
                .build();
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.numbers.size()", 5)
                .build();
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.person.size()", 0)
                .build();
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}

        validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.nullValue", "10")
                .build();
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
            Assert.fail("Missing validation exception");
        } catch (ValidationException e) {}
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithValidationMatcherNotSuccessful() {
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.attributeA", "@startsWith('attribute-')@");
        validationExpressions.put("$..element.attributeB", "@endsWith('-value')@");
        validationExpressions.put("$..element.sub-element", "@contains('FAIL')@");
        validationExpressions.put("$.root.nullValue", "@assertThat(noNullValue())@");

        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithJsonPathNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$..element.sub-element", "false-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithFullPathSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.element.sub-element", "text-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {ValidationException.class})
    public void testValidateMessageElementsWithFullPathNotSuccessful() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.element.sub-element", "false-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testValidateMessageElementsPathNotFound() {
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expression("$.root.foo", "foo-value")
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void testValidateMessageElementsWithMixedNotationsSuccessful() {
        //mix of xpath and dot-notation
        Map<String, Object> validationExpressions = new HashMap<>();
        validationExpressions.put("$..element.sub-element", "text-value");
        validationExpressions.put("$.root.element.sub-element", "text-value");

        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext.Builder()
                .expressions(validationExpressions)
                .build();

        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }

    @Test
    public void shouldFindProperValidationContext() {
        List<ValidationContext> validationContexts = new ArrayList<>();
        validationContexts.add(new HeaderValidationContext());
        validationContexts.add(new JsonMessageValidationContext());
        validationContexts.add(new XmlMessageValidationContext());
        validationContexts.add(new ScriptValidationContext(MessageType.JSON.name()));
        validationContexts.add(new ScriptValidationContext(MessageType.XML.name()));
        validationContexts.add(new ScriptValidationContext(MessageType.PLAINTEXT.name()));

        Assert.assertNull(validator.findValidationContext(validationContexts));

        validationContexts.add(new JsonPathMessageValidationContext());

        Assert.assertNotNull(validator.findValidationContext(validationContexts));
    }

    private static class NullValueMatcher implements ValidationMatcher {
        @Override
        public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
            if (controlParameters.get(0).equals("nullValue()")) {
                Assert.assertNull(value);
            } else if (controlParameters.get(0).equals("notNullValue()")) {
                Assert.assertNotNull(value);
            }
        }
    }
}
