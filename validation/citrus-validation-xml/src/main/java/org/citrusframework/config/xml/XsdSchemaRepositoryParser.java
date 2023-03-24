package org.citrusframework.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.xml.XsdSchemaRepository;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 */
public class XsdSchemaRepositoryParser extends AbstractBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(XsdSchemaRepository.class);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("schema-mapping-strategy"), "schemaMappingStrategy");
        SchemaRepositoryParser.addLocationsToBuilder(element, builder);
        SchemaRepositoryParser.parseSchemasElement(element, builder, parserContext);
        return builder.getBeanDefinition();
    }
}
