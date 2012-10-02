package com.consol.citrus.ssh.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Helper class for providing a mapping of XML attribute names to bean properties
 * and other utility stuff com for all config parser.
 *
 * @author roland
 * @since 11.09.12
 */
abstract public class AbstractSshParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(getBeanClass());

        // Direct properties
        String[] mapping = getAttributePropertyMapping();
        for (int i = 0;i < mapping.length; i+=2) {
            String value = element.getAttribute(mapping[i]);
            if (StringUtils.hasText(value)) {
                builder.addPropertyValue(mapping[i+1], value);
            }
        }

        // References
        mapping = getAttributePropertyReferenceMapping();
        for (int i = 0;i < mapping.length; i+=2) {
            String value = element.getAttribute(mapping[i]);
            if (StringUtils.hasText(value)) {
                builder.addPropertyReference(mapping[i+1], value);
            }
        }

        // Parse any extra information
        parseExtra(builder,element,parserContext);

        return builder.getBeanDefinition();
    }

    /**
     * Get a mapping from XML attribute names to bean properties name as to add
     * to the bean definition builder.
     *
     * @return attribute property mapping, must never be null. Single array, with odd elements
     * pointing to XML attribute names and even elements are the corresponding
     * property names.
     */
    protected abstract String[] getAttributePropertyMapping();

    /**
     * Return mappings for attrinute to bean reference names, which are used
     * to set a property reference
     * @return mapping for attrubute property reference mapping.
     */
    protected abstract String[] getAttributePropertyReferenceMapping();

    /**
     * Name of the bean class to instantiate.
     *
     * @return class name, must never be null
     */
    protected abstract Class getBeanClass();


    /**
     * Hook for doing extra initializations to the BeanDefinitionBuilder
     *
     * @param pBuilder builder to add values
     * @param pElement the XML element of the parsed config
     * @param pParserContext parser context
     */
    protected void parseExtra(BeanDefinitionBuilder pBuilder, Element pElement, ParserContext pParserContext) {
        // Empty by default
    }
}
