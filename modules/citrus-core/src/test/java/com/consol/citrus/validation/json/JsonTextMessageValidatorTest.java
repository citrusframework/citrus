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

import org.json.simple.parser.ParseException;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class JsonTextMessageValidatorTest extends AbstractBaseTest {

    @Test
    public void testJsonValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonValidationNestedObjects() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}").build();
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonValidationWithArrays() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"greetings\":[" +
        		"{\"text\":\"Hello World!\", \"index\":1}, " +
        		"{\"text\":\"Hallo Welt!\", \"index\":2}, " +
        		"{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"greetings\":[" +
        		"{\"text\":\"Hello World!\", \"index\":1}, " +
        		"{\"text\":\"Hallo Welt!\", \"index\":2}, " +
        		"{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonValidationVariableSupport() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello ${world}!\", \"index\":${index}, \"id\":\"${id}\"}").build();
        
        context.setVariable("world", "World");
        context.setVariable("index", "5");
        context.setVariable("id", "x123456789x");
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonValidationWrongNumberOfEntries() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\", \"missing\":\"this is missing\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
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
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
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
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"wrong\"}, \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
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
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":0}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
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
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}], \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 3"));
            Assert.assertTrue(e.getMessage().contains("but was 2"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong array size");
    }
    
    @Test
    public void testJsonValidationArrayTypeMismatch() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
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
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}], \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"greetings\":{\"text\":\"Hello World!\", \"index\":1}, \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'JSONObject'"));
            Assert.assertTrue(e.getMessage().contains("but was 'JSONArray'"));
            
            return;
        }
        
        Assert.fail("Missing validation exception due to type mismatch");
    }
    
    @Test
    public void testJsonValidationIgnoreEntries() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"object\":{\"id\":\"x123456789x\"}, \"greetings\":[" +
                "{\"text\":\"Hello World!\", \"index\":1}, " +
                "{\"text\":\"Hallo Welt!\", \"index\":2}, " +
                "{\"text\":\"Hola del mundo!\", \"index\":3}],}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":\"@ignore@\", \"object\":{\"id\":\"@ignore@\"}, \"greetings\":\"@ignore@\"}").build();
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonValidationInvalidJsonText() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"wrong\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":invalid, \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof ParseException);
            
            return;
        }
        
        Assert.fail("Missing validation exception due to wrong value");
    }
    
    @Test
    public void testJsonNullValueValidation() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}").build();
        
        validator.validateMessagePayload(receivedMessage, controlMessage, context);
    }
    
    @Test
    public void testJsonNullValueMismatch() {
        JsonTextMessageValidator validator = new JsonTextMessageValidator();
        
        Message<String> receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        Message<String> controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
            Assert.fail("Missing validation exception due to wrong value");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'null' but was 'x123456789x'"));
        }
        
        receivedMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":null}").build();
        controlMessage = MessageBuilder.withPayload("{\"text\":\"Hello World!\", \"index\":5, \"id\":\"x123456789x\"}").build();
        
        try {
            validator.validateMessagePayload(receivedMessage, controlMessage, context);
            Assert.fail("Missing validation exception due to wrong value");
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("expected 'x123456789x' but was 'null'"));
        }
    }
}
