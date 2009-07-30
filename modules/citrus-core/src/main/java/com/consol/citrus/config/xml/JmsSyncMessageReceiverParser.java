package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class JmsSyncMessageReceiverParser extends AbstractJmsConfigParser {

    @Override
    protected BeanDefinitionBuilder doParse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition("com.consol.citrus.jms.JmsSyncMessageReceiver");
        
        return builder;
    }

}
