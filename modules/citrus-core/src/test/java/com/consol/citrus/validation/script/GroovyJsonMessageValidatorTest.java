/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.validation.script;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author DanielP
 */
public class GroovyJsonMessageValidatorTest extends AbstractTestNGUnitTest {

    private GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        Message<?> message = MessageBuilder.withPayload("{\"person\":{\"name\":\"Christoph\",\"age\":31," +
        		"\"pets\":[\"dog\",\"cat\"]}}").build();
        
        String validationScript = "assert json.size() == 1 \n" +
                                  "assert json.person.name == 'Christoph' \n" +
                                  "assert json.person.age == 31 \n" +
                                  "assert json.person.pets.size() == 2 \n" +
                                  "assert json.person.pets[0] == 'dog' \n" +
                                  "assert json.person.pets[1] == 'cat' \n";

        ScriptValidationContext validationContext = new ScriptValidationContext(validationScript, 
                                                                                ScriptTypes.GROOVY);

        validator.validateMessage(message, context, validationContext);
        
        validationScript += "assert json.person.age == 32";
        validationContext.setValidationScript(validationScript);
        
        try {
            validator.validateMessage(message, context, validationContext);
            Assert.fail("Missing validation exception for groovy JSON slurper.");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("assert json.person.age == 32"));
            Assert.assertTrue(e.getMessage().contains("31  false"));
        }
    }

}
