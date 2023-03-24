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

import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for plsql action in test case.
 *
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecutePLSQLActionFactoryBean.class);

        String dataSource = element.getAttribute("datasource");
        beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + dataSource);
        beanDefinition.addPropertyReference("dataSource", dataSource);

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("transaction-manager"), "transactionManager");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("transaction-timeout"), "transactionTimeout");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("transaction-isolation-level"), "transactionIsolationLevel");

        DescriptionElementParser.doParse(element, beanDefinition);

        Element scriptElement = DomUtils.getChildElementByTagName(element, "script");
        if (scriptElement != null) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(scriptElement).trim());
        }

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            beanDefinition.addPropertyValue("sqlResourcePath", sqlResourceElement.getAttribute("file"));
        }

        String ignoreErrors = element.getAttribute("ignore-errors");
        if (ignoreErrors != null && ignoreErrors.equals("true")) {
            beanDefinition.addPropertyValue("ignoreErrors", true);
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class ExecutePLSQLActionFactoryBean extends AbstractDatabaseConnectingTestActionFactoryBean<ExecutePLSQLAction, ExecutePLSQLAction.Builder> {

        private final ExecutePLSQLAction.Builder builder = new ExecutePLSQLAction.Builder();

        @Override
        public ExecutePLSQLAction getObject() throws Exception {
            return builder.build();
        }

        /**
         * Ignore errors during execution.
         * @param ignoreErrors boolean flag to set
         */
        public void setIgnoreErrors(boolean ignoreErrors) {
            builder.ignoreErrors(ignoreErrors);
        }

        /**
         * The PLSQL script.
         * @param script the plsql script.
         */
        public void setScript(String script) {
            builder.sqlScript(script);
        }

        @Override
        public Class<?> getObjectType() {
            return ExecutePLSQLAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ExecutePLSQLAction.Builder getBuilder() {
            return builder;
        }
    }
}
