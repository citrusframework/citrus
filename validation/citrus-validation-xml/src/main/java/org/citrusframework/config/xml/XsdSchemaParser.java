package org.citrusframework.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 */
public class XsdSchemaParser extends AbstractBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String location = element.getAttribute("location");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleXsdSchema.class);
        BeanDefinitionParserUtils.setPropertyValue(builder, location, "xsd");
        return builder.getBeanDefinition();
    }
}
