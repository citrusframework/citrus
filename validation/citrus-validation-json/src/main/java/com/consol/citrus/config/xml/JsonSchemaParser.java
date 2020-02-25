package com.consol.citrus.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 */
public class JsonSchemaParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        String location = element.getAttribute("location");

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleJsonSchema.class);
        BeanDefinitionParserUtils.setPropertyValue(builder, location, "json");
        return builder.getBeanDefinition();
    }
}
