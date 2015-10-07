/*
 * Copyright 2006-2010 the original author or authors.
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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyXmlMessageValidatorTest extends AbstractTestNGUnitTest {

    private GroovyXmlMessageValidator validator = new GroovyXmlMessageValidator();
    
    private Message message;

    @BeforeMethod
    public void prepareTestData() {
        message = new DefaultMessage("<RequestMessage Id=\"123456789\" xmlns=\"http://citrus/test\">"
                + "<CorrelationId>Kx1R123456789</CorrelationId>"
                + "<BookingId>Bx1G987654321</BookingId>"
                + "<Text>Hello TestFramework</Text>"
            + "</RequestMessage>");
    }
    
    @Test
    public void testGroovyScriptValidation() throws ValidationException {
        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == 'Kx1R123456789' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text.text() == 'Hello TestFramework'";
                        
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript(validationScript);
        
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
    
    @Test
    public void testGroovyScriptValidationVariableSupport() {
        context.setVariable("user", "TestFramework");
        context.setVariable("correlationId", "Kx1R123456789");
        
        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == '${correlationId}' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text.text() == 'Hello ' + context.getVariable(\"user\")";
                        
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript(validationScript);
        
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
    
    @Test
    public void testGroovyScriptValidationFailed() {
        String validationScript = "assert root.children().size() == 3 \n" +
                        "assert root.CorrelationId.text() == 'Kx1R123456789' \n" +
                        "assert root.BookingId.text() == 'Bx1G987654321' \n" +
                        "assert root.Text == 'Hello Citrus'"; //should fail
                        
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript(validationScript);
        
        try {
            validator.validateMessage(message, new DefaultMessage(), context, validationContext);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Groovy script validation failed"));
            Assert.assertTrue(e.getMessage().contains("Hello Citrus"));
            Assert.assertTrue(e.getMessage().contains("Hello TestFramework"));
            return;
        }
        
        Assert.fail("Missing script validation exception caused by wrong control value");
    }
    
    @Test
    public void testEmptyValidationScript() {
        String validationScript = "";
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript(validationScript);
        
        validator.validateMessage(message, new DefaultMessage(), context, validationContext);
    }
}
