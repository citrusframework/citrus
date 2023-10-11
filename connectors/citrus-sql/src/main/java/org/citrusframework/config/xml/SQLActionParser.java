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

package org.citrusframework.config.xml;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for sql action in test case.
 *
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SQLActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        String dataSource = element.getAttribute("datasource");
        if (!StringUtils.hasText(dataSource)) {
            throw new BeanCreationException("Missing proper data source reference");
        }

        List<Element> validateElements = DomUtils.getChildElementsByTagName(element, "validate");
        List<Element> extractElements = DomUtils.getChildElementsByTagName(element, "extract");
        Element scriptValidationElement = DomUtils.getChildElementByTagName(element, "validate-script");

        if (validateElements.isEmpty() && extractElements.isEmpty() && scriptValidationElement == null) {
            beanDefinition = parseSqlAction(element);
            beanDefinition.addPropertyValue("name", "sqlUpdate:" + dataSource);
        } else {
            beanDefinition = parseSqlQueryAction(element, scriptValidationElement, validateElements, extractElements);
            beanDefinition.addPropertyValue("name", "sqlQuery:" + dataSource);
        }

        beanDefinition.addPropertyReference("dataSource", dataSource);

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("transaction-manager"), "transactionManager");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("transaction-timeout"), "transactionTimeout");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("transaction-isolation-level"), "transactionIsolationLevel");

        DescriptionElementParser.doParse(element, beanDefinition);

        List<String> statements = new ArrayList<String>();
        List<Element> stmtElements = DomUtils.getChildElementsByTagName(element, "statement");
        for (Element stmt : stmtElements) {
            statements.add(DomUtils.getTextValue(stmt));
        }
        beanDefinition.addPropertyValue("statements", statements);

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            beanDefinition.addPropertyValue("sqlResourcePath", sqlResourceElement.getAttribute("file"));
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Parses SQL action just executing a set of statements.
     * @param element
     * @return
     */
    private BeanDefinitionBuilder parseSqlAction(Element element) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLActionFactoryBean.class);

        String ignoreErrors = element.getAttribute("ignore-errors");
        if (ignoreErrors != null && ignoreErrors.equals("true")) {
            beanDefinition.addPropertyValue("ignoreErrors", true);
        }

        return beanDefinition;
    }

    /**
     * Parses SQL query action with result set validation elements.
     * @param element the root element.
     * @param scriptValidationElement the optional script validation element.
     * @param validateElements validation elements.
     * @param extractElements variable extraction elements.
     * @return
     */
    private BeanDefinitionBuilder parseSqlQueryAction(Element element, Element scriptValidationElement,
            List<Element> validateElements, List<Element> extractElements) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLQueryActionFactoryBean.class);

        // check for script validation
        if (scriptValidationElement != null) {
            beanDefinition.addPropertyValue("scriptValidationContext", getScriptValidationContext(scriptValidationElement));
        }

        Map<String, List<String>> controlResultSet = new HashMap<>();
        for (Element validateElement : validateElements) {
            Element valueListElement = DomUtils.getChildElementByTagName(validateElement, "values");

            if (valueListElement != null) {
                List<String> valueList = new ArrayList<>();
                List<Element> valueElements = DomUtils.getChildElementsByTagName(valueListElement, "value");
                for (Element valueElement : valueElements) {
                    valueList.add(DomUtils.getTextValue(valueElement));
                }
                controlResultSet.put(validateElement.getAttribute("column"), valueList);
            } else if (validateElement.hasAttribute("value")) {
                controlResultSet.put(validateElement.getAttribute("column"), Collections.singletonList(validateElement.getAttribute("value")));
            } else {
                throw new BeanCreationException(element.getLocalName(),
                        "Neither value attribute nor value list is set for column validation: " + validateElement.getAttribute("column"));
            }
        }

        beanDefinition.addPropertyValue("controlResultSet", controlResultSet);

        Map<String, String> extractVariables = new HashMap<>();
        for (Element validate : extractElements) {
            extractVariables.put(validate.getAttribute("column"), validate.getAttribute("variable"));
        }

        beanDefinition.addPropertyValue("extractVariables", extractVariables);

        return beanDefinition;
    }

    /**
     * Constructs the script validation context.
     * @param scriptElement
     * @return
     */
    private ScriptValidationContext getScriptValidationContext(Element scriptElement) {
        String type = scriptElement.getAttribute("type");

        ScriptValidationContext.Builder validationContext = new ScriptValidationContext.Builder()
                .scriptType(type);
        String filePath = scriptElement.getAttribute("file");
        if (StringUtils.hasText(filePath)) {
            validationContext.scriptResource(filePath);
        } else {
            validationContext.script(DomUtils.getTextValue(scriptElement));
        }

        return validationContext.build();
    }

    /**
     * Test action factory bean.
     */
    public static class ExecuteSQLActionFactoryBean extends AbstractDatabaseConnectingTestActionFactoryBean<ExecuteSQLAction, ExecuteSQLAction.Builder> {

        private final ExecuteSQLAction.Builder builder = new ExecuteSQLAction.Builder();

        @Override
        public ExecuteSQLAction getObject() throws Exception {
            return builder.build();
        }

        /**
         * Ignore errors during execution.
         * @param ignoreErrors boolean flag to set
         */
        public void setIgnoreErrors(boolean ignoreErrors) {
            builder.ignoreErrors(ignoreErrors);
        }

        @Override
        public Class<?> getObjectType() {
            return ExecuteSQLAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ExecuteSQLAction.Builder getBuilder() {
            return builder;
        }
    }

    /**
     * Test action factory bean.
     */
    public static class ExecuteSQLQueryActionFactoryBean extends AbstractDatabaseConnectingTestActionFactoryBean<ExecuteSQLQueryAction, ExecuteSQLQueryAction.Builder> {

        private final ExecuteSQLQueryAction.Builder builder = new ExecuteSQLQueryAction.Builder();

        @Override
        public ExecuteSQLQueryAction getObject() throws Exception {
            return builder.build();
        }

        /**
         * Set expected control result set. Keys represent the column names, values
         * the expected values.
         * @param controlResultSet
         */
        public void setControlResultSet(Map<String, List<String>> controlResultSet) {
            controlResultSet.forEach((key, value) -> builder.validate(key, value.toArray(new String[0])));
        }

        /**
         * User can extract column values to test variables. Map holds column names (keys) and
         * respective target variable names (values).
         * @param variablesMap the variables to be created out of database values
         */
        public void setExtractVariables(Map<String, String> variablesMap) {
            variablesMap.forEach(builder::extract);
        }

        /**
         * Sets the script validation context.
         * @param scriptValidationContext the scriptValidationContext to set
         */
        public void setScriptValidationContext(ScriptValidationContext scriptValidationContext) {
            if (scriptValidationContext.getValidationScript() != null) {
                builder.validateScript(scriptValidationContext.getValidationScript(), scriptValidationContext.getScriptType());
            }

            if (scriptValidationContext.getValidationScriptResourcePath() != null) {
                builder.validateScriptResource(scriptValidationContext.getValidationScriptResourcePath(),
                        scriptValidationContext.getScriptType(),
                        Charset.forName(scriptValidationContext.getValidationScriptResourceCharset()));
            }
        }

        @Override
        public Class<?> getObjectType() {
            return ExecuteSQLQueryAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ExecuteSQLQueryAction.Builder getBuilder() {
            return builder;
        }
    }
}
