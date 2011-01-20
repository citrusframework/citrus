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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.List;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.context.ValidationContext;

/**
 * Groovy script message validator passing the message to a validation script.
 * Tester needs to write validation code in Groovy.
 * 
 * Available objects inside groovy script are 'receivedMessage' which is the actual {@link Message} object
 * to validate and 'context' the current {@link TestContext}.
 * 
 * @author Christoph Deppisch
 */
public class GroovyScriptMessageValidator extends AbstractMessageValidator<ScriptValidationContext> {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GroovyScriptMessageValidator.class);
    
    /** Static code snippet for groovy script validation */
    private Resource scriptTemplateResource;
    
    /**
     * Default constructor using default script template.
     */
    public GroovyScriptMessageValidator() {
        this(new ClassPathResource("com/consol/citrus/validation/script-validation-template.groovy"));
    }
    
    /**
     * Constructor setting the script template for this validator.
     * @param scriptTemplateResource the script template to use in this validator.
     */
    public GroovyScriptMessageValidator(ClassPathResource scriptTemplateResource) {
        this.scriptTemplateResource = scriptTemplateResource;
    }

    /**
     * Validates the message with test context and script validation context.
     */
    public void validateMessage(Message<?> receivedMessage, TestContext context, ScriptValidationContext validationContext) 
        throws ValidationException {
        try {
            String validationScript = validationContext.getValidationScript(context);
            
            if (StringUtils.hasText(validationScript)) {
                log.info("Start groovy message validation");
                
                GroovyClassLoader loader = new GroovyClassLoader(GroovyScriptMessageValidator.class.getClassLoader());
                Class<?> groovyClass = loader.parseClass(TemplateBasedScriptBuilder.fromTemplateResource(scriptTemplateResource)
                                                            .withCode(validationScript)
                                                            .build());
                
                if (groovyClass == null) {
                    throw new CitrusRuntimeException("Failed to load groovy validation script resource");
                }
                
                GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                ((ValidationScriptExecutor) groovyObject).validate(receivedMessage, context);
                
                log.info("Groovy message validation finished successfully: All values OK");
            }
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (AssertionError e) {
            throw new ValidationException("Groovy script validation failed with assertion error:\n" + e.getMessage(), e);
        }
    }

    /**
     * Executes a validation-script
     */
     public static interface ValidationScriptExecutor {
         public void validate(Message<?> receivedMessage, TestContext context);
     }

    /**
     * Returns the needed validation context for this validation mechanism.
     */
    public ScriptValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (validationContext instanceof ScriptValidationContext) {
                if (((ScriptValidationContext)validationContext).getScriptType().equals(ScriptTypes.GROOVY)) {
                    return (ScriptValidationContext) validationContext;
                }
            }
        }
        
        return null;
    }

    /**
     * Checks if the message type is supported. 
     */
    public boolean supportsMessageType(String messageType) {
        // support all message types other than xml
        return !messageType.equalsIgnoreCase("xml");
    }
}
