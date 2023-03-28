package org.citrusframework.config.xml;

import org.citrusframework.json.JsonSchemaRepository;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 */
public class JsonSchemaRepositoryParser extends AbstractBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonSchemaRepository.class);
        SchemaRepositoryParser.addLocationsToBuilder(element, builder);
        SchemaRepositoryParser.parseSchemasElement(element, builder, parserContext);
        return builder.getBeanDefinition();
    }
}
