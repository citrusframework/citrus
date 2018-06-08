/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.config.xml;

import com.consol.citrus.condition.*;
import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.container.Wait;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Bean definition parser for wait action in test case.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class WaitParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Wait.class);

        DescriptionElementParser.doParse(element, builder);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("seconds"), "seconds");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("milliseconds"), "milliseconds");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("interval"), "interval");

        Element conditionElement = DOMUtil.getFirstChildElement(element);
        if (conditionElement != null && conditionElement.getTagName().equals("description")) {
            conditionElement = DOMUtil.getNextSiblingElement(conditionElement);
        }

        if (conditionElement == null) {
            throw new CitrusRuntimeException("Invalid 'wait' action configuration. No 'condition' is configured");
        } else {
            String conditionName = conditionElement.getTagName();
            Object condition = null;
            switch (conditionName) {
                case "http":
                    condition = parseHttpCondition(conditionElement);
                    break;
                case "file":
                    condition = parseFileCondition(conditionElement);
                    break;
                case "message":
                    condition = parseMessageCondition(conditionElement);
                    break;
                case "action":
                    builder.addPropertyValue("action", parseActionCondition(conditionElement, parserContext));
                    break;
                default:
                    throw new CitrusRuntimeException(String.format("Invalid 'wait' action configuration. Unknown condition '%s'", conditionName));
            }

            if (condition != null) {
                builder.addPropertyValue("condition", condition);
            }
        }
        return builder.getBeanDefinition();
    }

    /**
     * Parse Http request condition.
     * @param element
     * @return
     */
    private Condition parseHttpCondition(Element element) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(element.getAttribute("url"));

        String method = element.getAttribute("method");
        if (StringUtils.hasText(method)) {
            condition.setMethod(method);
        }

        String statusCode = element.getAttribute("status");
        if (StringUtils.hasText(statusCode)) {
            condition.setHttpResponseCode(statusCode);
        }

        String timeout = element.getAttribute("timeout");
        if (StringUtils.hasText(timeout)) {
            condition.setTimeout(timeout);
        }
        return condition;
    }

    /**
     * Parse message store condition.
     * @param element
     * @return
     */
    private Condition parseMessageCondition(Element element) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(element.getAttribute("name"));

        return condition;
    }

    /**
     * Parse test action condition.
     * @param element
     * @param parserContext
     * @return
     */
    private BeanDefinition parseActionCondition(Element element, ParserContext parserContext) {
        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element action = DOMUtil.getFirstChildElement(element);
        if (action != null) {
            BeanDefinitionParser parser = actionRegistry.get(action.getTagName());

            if (parser !=  null) {
                return parser.parse(action, parserContext);
            } else {
                return parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext);
            }
        }

        throw new BeanCreationException("Invalid wait for action condition - action not set properly");
    }

    /**
     * Parse file existence condition.
     * @param element
     * @return
     */
    private Condition parseFileCondition(Element element) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(element.getAttribute("path"));
        return condition;
    }
}
