/*
 * Copyright the original author or authors.
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

package org.citrusframework.validation.script;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * Groovy script message validator passing the message to a validation script.
 * Tester needs to write validation code in Groovy.
 *
 * Available objects inside groovy script are 'receivedMessage' which is the actual {@link Message} object
 * to validate and 'context' the current {@link TestContext}.
 *
 */
public class GroovyScriptMessageValidator extends AbstractMessageValidator<ScriptValidationContext> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptMessageValidator.class);

    /** Static code snippet for groovy script validation */
    private final Resource scriptTemplateResource;

    /**
     * Default constructor using default script template.
     */
    public GroovyScriptMessageValidator() {
        this(Resources.fromClasspath("org/citrusframework/validation/script-validation-template.groovy"));
    }

    /**
     * Constructor setting the script template for this validator.
     * @param scriptTemplateResource the script template to use in this validator.
     */
    public GroovyScriptMessageValidator(Resource scriptTemplateResource) {
        this.scriptTemplateResource = scriptTemplateResource;
    }

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, ScriptValidationContext validationContext)
        throws ValidationException {
        try {
            String validationScript = validationContext.getValidationScript(context);

            if (StringUtils.hasText(validationScript)) {
                logger.debug("Start groovy message validation ...");

                try (var loader = getPrivilegedGroovyLoader()) {
                    Class<?> groovyClass = loader.parseClass(TemplateBasedScriptBuilder.fromTemplateResource(scriptTemplateResource)
                            .withCode(validationScript)
                            .build());

                    if (groovyClass == null) {
                        throw new CitrusRuntimeException("Failed to load groovy validation script resource");
                    }

                    GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                    ((GroovyScriptExecutor) groovyObject).validate(receivedMessage, context);
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to load groovy validation script resource", e);
                }

                logger.debug("Groovy message validation successful: All values OK");
            }
        } catch (CompilationFailedException | InstantiationException | IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (AssertionError e) {
            throw new ValidationException("Groovy script validation failed with assertion error:\n" + e.getMessage(), e);
        }
    }

    private static GroovyClassLoader getPrivilegedGroovyLoader() {
        return AccessController.doPrivileged((PrivilegedAction<GroovyClassLoader>) () -> new GroovyClassLoader(GroovyScriptMessageValidator.class.getClassLoader()));
    }

    @Override
    public ScriptValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (getRequiredValidationContextType().isInstance(validationContext) &&
                    ((ScriptValidationContext)validationContext).getScriptType().equals(ScriptTypes.GROOVY)) {
                return (ScriptValidationContext) validationContext;
            }
        }

        return null;
    }

    @Override
    protected Class<ScriptValidationContext> getRequiredValidationContextType() {
        return ScriptValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        // only support plaintext message type
        return messageType.equalsIgnoreCase(MessageType.PLAINTEXT.toString());
    }
}
