/*
 * Copyright 2006-2011 the original author or authors.
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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.json.report.GraciousProcessingReport;
import com.consol.citrus.validation.json.schema.JsonSchemaValidation;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JsonTextMessageValidatorTest extends AbstractTestNGUnitTest {

    @Autowired
    @Qualifier("defaultJsonMessageValidator")
    private JsonTextMessageValidator validator;

    @Test
    public void testJsonValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();

        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testSloppyJsonValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator().strict(false);

        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"id\":\"x123456789x\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testJsonValidationNestedObjects() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonValidationWithArrays() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();

        Message receivedMessage = new DefaultMessage("[" +
        		"{\"text\":\"Hello World!\", \"index\":1}, " +
        		"{\"text\":\"Hallo Welt!\", \"index\":2}, " +
        		"{\"text\":\"Hola del mundo!\", \"index\":3}]");
        Message controlMessage = new DefaultMessage("[" +
        		"{\"text\":\"Hello World!\", \"index\":1}, " +
        		"{\"text\":\"Hallo Welt!\", \"index\":2}, " +
        		"{\"text\":\"Hola del mundo!\", \"index\":3}]");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testSloppyJsonValidationWithArrays() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        validator.setStrict(false);

        Message receivedMessage = new DefaultMessage("[" +
        		"{\"text\":\"Hello World!\", \"index\":1}, " +
        		"{\"text\":\"Hallo Welt!\", \"index\":2}, " +
        		"{\"text\":\"Hola del mundo!\", \"index\":3}]");
        Message controlMessage = new DefaultMessage("[{\"text\":\"Hello World!\", \"index\":1}] ");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testJsonValidationWithNestedArrays() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonValidationVariableSupport() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello ${world}!\", \"index\":${index}, \"id\":\"${id}\"}");
        
        context.setVariable("world", "World");
        context.setVariable("index", "5");
        context.setVariable("id", "x123456789x");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonValidationWrongNumberOfEntries() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\", \"missing\":\"this is missing\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected '4'"));
            Assert.assertTrue(e.getMessage().contains("but was '3'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong number of JSON entries");
    }
    
    @Test
    public void testJsonValidationWrongValue() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'x123456789x'"));
            Assert.assertTrue(e.getMessage().contains("but was 'wrong'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong value");
    }
    
    @Test
    public void testJsonValidationWrongValueInNestedObjects() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"wrong\"}, \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'Doe'"));
            Assert.assertTrue(e.getMessage().contains("but was 'wrong'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong value");
    }
    
    @Test
    public void testJsonValidationWrongValueInArrays() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":0}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected '2'"));
            Assert.assertTrue(e.getMessage().contains("but was '0'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong value");
    }
    
    @Test
    public void testJsonValidationWrongArraySize() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}], \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected '3'"));
            Assert.assertTrue(e.getMessage().contains("but was '2'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong array size");
    }
    
    @Test
    public void testJsonValidationArrayTypeMismatch() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'JSONArray'"));
            Assert.assertTrue(e.getMessage().contains("but was 'JSONObject'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to type mismatch");
    }
    
    @Test
    public void testJsonValidationObjectTypeMismatch() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'JSONObject'"));
            Assert.assertTrue(e.getMessage().contains("but was 'JSONArray'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to type mismatch");
    }
    
    @Test
    public void testJsonValidationIgnorePlaceholder() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"object\":{\"id\":\"x123456789x\"}, \"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}],}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":\"@ignore@\", \"object\":{\"id\":\"@ignore@\"}, \"greetings\":\"@ignore@\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testJsonValidationIgnoreEntries() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();

        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"object\":{\"id\":\"x123456789x\"}, \"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}],}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":\"?\", \"object\":{\"id\":\"?\"}, \"greetings\":\"?\"}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.getIgnoreExpressions().add("$..index");
        validationContext.getIgnoreExpressions().add("$.object.id");
        validationContext.getIgnoreExpressions().add("$.greetings");
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonValidationInvalidJsonText() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":invalid, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof ParseException);
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong value");
    }
    
    @Test
    public void testJsonNullValueValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonEmptyMessageValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("");
        Message controlMessage = new DefaultMessage("");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonEmptyMessageValidationError() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing validation exception due to validation error");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected message contents, but received empty message"));
        }
        
        receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        controlMessage = new DefaultMessage("");

        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }
    
    @Test
    public void testJsonNullValueMismatch() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing validation exception due to wrong value");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'null' but was 'x123456789x'"));
        }
        
        receivedMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}");
        controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}");
        
        try {
            JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
            Assert.fail("Missing validation exception due to wrong value");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'x123456789x' but was 'null'"));
        }
    }

    @Test
    public void testUseSchemaRepositoryValidatorIfSchemaValidationIsEnabled() {

        //GIVEN
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setSchemaValidation(true);

        JsonSchemaValidation jsonSchemaValidation = mock(JsonSchemaValidation.class);
        when(jsonSchemaValidation.validate(any(), anyList(), any(), any())).thenReturn(new GraciousProcessingReport((true)));
        validator.setJsonSchemaValidation(jsonSchemaValidation);

        JsonSchemaRepository jsonSchemaRepository = mock(JsonSchemaRepository.class);
        validator.setSchemaRepositories(Collections.singletonList(jsonSchemaRepository));

        Message receivedMessage = new DefaultMessage("{\"id\":42}");
        Message controlMessage = new DefaultMessage("{\"id\":42}");

        //WHEN
        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);

        //THEN
        verify(jsonSchemaValidation).validate(eq(receivedMessage), anyList(), eq(validationContext), eq(applicationContext));
    }
}
