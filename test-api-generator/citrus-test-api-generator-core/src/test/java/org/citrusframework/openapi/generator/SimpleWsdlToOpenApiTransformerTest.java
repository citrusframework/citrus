package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.util.FileUtils.readToString;

import java.io.IOException;
import org.citrusframework.openapi.generator.exception.WsdlToOpenApiTransformationException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SimpleWsdlToOpenApiTransformerTest {

    @Test
    void testTransform() throws WsdlToOpenApiTransformationException, IOException {
        ClassPathResource wsdlResource = new ClassPathResource(
            "/org/citrusframework/openapi/generator/SimpleWsdlToOpenApiTransformerTest/BookService.wsdl");

        SimpleWsdlToOpenApiTransformer simpleWsdlToOpenApiTransformer = new SimpleWsdlToOpenApiTransformer(wsdlResource.getURI());
        String generatedYaml = simpleWsdlToOpenApiTransformer.transformToOpenApi();

        Resource expectedYamlResource = new ClasspathResource(
            "/org/citrusframework/openapi/generator/SimpleWsdlToOpenApiTransformerTest/BookService-generated.yaml");

        String expectedYaml = readToString(expectedYamlResource);
        assertThat(generatedYaml).isEqualToIgnoringWhitespace(expectedYaml);
    }
}
