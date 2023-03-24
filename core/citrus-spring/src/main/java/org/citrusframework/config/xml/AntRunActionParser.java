/*
 * Copyright 2006-2013 the original author or authors.
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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.citrusframework.actions.AntRunAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.apache.tools.ant.BuildListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for ant run action in test case.
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public class AntRunActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AntRunActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("build-file"), "buildFilePath");

        Element executeElement = DomUtils.getChildElementByTagName(element, "execute");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, executeElement.getAttribute("target"), "target");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, executeElement.getAttribute("targets"), "targets");

        Properties properties = new Properties();
        Element propertiesElement = DomUtils.getChildElementByTagName(element, "properties");
        if (propertiesElement != null) {
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, propertiesElement.getAttribute("file"), "propertyFilePath");

            List<?> propertyElements = DomUtils.getChildElementsByTagName(propertiesElement, "property");
            if (propertyElements.size() > 0) {
                for (Iterator<?> iter = propertyElements.iterator(); iter.hasNext();) {
                    Element propertyElement = (Element) iter.next();
                    properties.put(propertyElement.getAttribute("name"), propertyElement.getAttribute("value"));
                }

                beanDefinition.addPropertyValue("properties", properties);
            }
        }

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("build-listener"), "buildListener");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class AntRunActionFactoryBean extends AbstractTestActionFactoryBean<AntRunAction, AntRunAction.Builder> {

        private final AntRunAction.Builder builder = new AntRunAction.Builder();

        /**
         * Sets the buildFilePath.
         * @param buildFilePath the buildFilePath to set
         */
        public void setBuildFilePath(String buildFilePath) {
            builder.buildFilePath(buildFilePath);
        }

        /**
         * Sets the target.
         * @param target the target to set
         */
        public void setTarget(String target) {
            builder.target(target);
        }

        /**
         * Sets the targets.
         * @param targets the targets to set
         */
        public void setTargets(String targets) {
            builder.targets(targets);
        }

        /**
         * Sets the properties.
         * @param properties the properties to set
         */
        public void setProperties(Properties properties) {
            builder.properties(properties);
        }

        /**
         * Sets the propertyFilePath.
         * @param propertyFilePath the propertyFilePath to set
         */
        public void setPropertyFilePath(String propertyFilePath) {
            builder.propertyFile(propertyFilePath);
        }

        /**
         * Sets the buildListener.
         * @param buildListener the buildListener to set
         */
        public void setBuildListener(BuildListener buildListener) {
            builder.listener(buildListener);
        }

        @Override
        public AntRunAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return AntRunAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public AntRunAction.Builder getBuilder() {
            return builder;
        }
    }
}
