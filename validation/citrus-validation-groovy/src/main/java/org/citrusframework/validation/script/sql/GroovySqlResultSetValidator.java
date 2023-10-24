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

package org.citrusframework.validation.script.sql;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.script.GroovyScriptMessageValidator;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.validation.script.TemplateBasedScriptBuilder;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Groovy script validator capable of validating SQL result sets.
 *
 * @author Christoph Deppisch
 */
public class GroovySqlResultSetValidator implements SqlResultSetScriptValidator {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(GroovySqlResultSetValidator.class);

    /** Static code snippet for groovy script validation */
    private final Resource scriptTemplateResource;

    /**
     * Default constructor using a default script template.
     */
    public GroovySqlResultSetValidator() {
        this(Resources.fromClasspath("org/citrusframework/validation/sql/sql-validation-template.groovy"));
    }

    /**
     * Constructor with script template.
     * @param scriptTemplateResource
     */
    public GroovySqlResultSetValidator(Resource scriptTemplateResource) {
        this.scriptTemplateResource = scriptTemplateResource;
    }

    public void validateSqlResultSet(List<Map<String, Object>> resultSet,
            ScriptValidationContext validationContext, TestContext context)
            throws ValidationException {
        // only validate if groovy script type is set
        if (validationContext.getScriptType().equals(ScriptTypes.GROOVY)) {
            try {
                String validationScript = validationContext.getValidationScript(context);

                if (StringUtils.hasText(validationScript)) {
                    logger.debug("Start groovy SQL result set validation");

                    GroovyClassLoader loader = new GroovyClassLoader(GroovyScriptMessageValidator.class.getClassLoader());
                    Class<?> groovyClass = loader.parseClass(TemplateBasedScriptBuilder.fromTemplateResource(scriptTemplateResource)
                                                                .withCode(validationScript)
                                                                .build());

                    if (groovyClass == null) {
                        throw new CitrusRuntimeException("Failed to load groovy validation script resource");
                    }

                    GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
                    ((SqlResultSetScriptExecutor) groovyObject).validate(resultSet, context);

                    logger.info("Groovy SQL result set validation successful: All values OK");
                }
            } catch (CompilationFailedException | InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new CitrusRuntimeException(e);
            } catch (AssertionError e) {
                throw new ValidationException("Groovy SQL result set validation failed with assertion error:\n" + e.getMessage(), e);
            }
        }
    }
}
