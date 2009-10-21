/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public class JmsSyncMessageReceiverParser extends AbstractJmsConfigParser {

    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.jms.JmsSyncMessageReceiver");
        
        String replyTimeout = element.getAttribute(JmsParserConstants.RECEIVE_TIMEOUT_ATTRIBUTE);
        
        if (StringUtils.hasText(replyTimeout)) {
            builder.addPropertyValue(JmsParserConstants.RECEIVE_TIMEOUT_PROPERTY, replyTimeout);
        }
        
        String replyMessageCorrelator = element.getAttribute(JmsParserConstants.REPLY_CORRELATOR_ATTRIBUTE);
        
        if (StringUtils.hasText(replyMessageCorrelator)) {
            builder.addPropertyReference(JmsParserConstants.REPLY_CORRELATOR_PROPERTY, replyMessageCorrelator);
        }
        
        return builder;
    }

}
