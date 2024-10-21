package org.citrusframework.openapi.generator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.citrusframework.openapi.generator.CitrusJavaCodegen.CustomCodegenOperation;
import org.citrusframework.openapi.generator.CitrusJavaCodegen.CustomCodegenParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.codegen.CodegenConfigLoader;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.CODEGEN_NAME;

/**
 * This test validates the code generation process.
 */
class CitrusJavaCodegenTest {

    private CitrusJavaCodegen codegen;

    @BeforeEach
    void setUp() {
        codegen = new CitrusJavaCodegen();
    }

    @Test
    void retrieveGeneratorBySpi() {
        CitrusJavaCodegen codegen = (CitrusJavaCodegen) CodegenConfigLoader.forName("java-citrus");
        assertThat(codegen).isNotNull();
    }

    @Test
    void arePredefinedValuesNotEmptyTest() {
        CitrusJavaCodegen codegen = new CitrusJavaCodegen();

        assertThat(codegen.getName()).isEqualTo(CODEGEN_NAME);
        assertThat(codegen.getHelp()).isNotEmpty();
        assertThat(codegen.getHttpClient()).isNotEmpty();
        assertThat(codegen.getApiPrefix()).isNotEmpty();
        assertThat(codegen.getTargetXmlnsNamespace()).isNull();
        assertThat(codegen.getGeneratedSchemaFolder()).isNotEmpty();
    }

    @Test
    void testGetName() {
        assertThat(codegen.getName()).isEqualTo("java-citrus");
    }

    @Test
    void testGetHelp() {
        String helpMessage = codegen.getHelp();
        assertThat(helpMessage).isEqualTo("Generates citrus api requests.");
    }

    @Test
    void testAdditionalPropertiesConfiguration() {
        assertThat(codegen.additionalProperties())
                .containsEntry("apiVersion", "1.0.0")
                .containsEntry(CitrusJavaCodegen.API_TYPE, CitrusJavaCodegen.API_TYPE_REST)
                .containsEntry("useJakartaEe", true);
    }

    @Test
    void testReservedWordsConfiguration() {
        assertThat(codegen.reservedWords())
                .contains("name", "description", "httpclient")
                .doesNotContain("nonReservedWord");
    }

    @Test
    void testTypeMappings() {
        assertThat(codegen.typeMapping())
                .containsEntry("binary", "Resource")
                .containsEntry("file", "Resource");
    }

    @Test
    void testProcessOptsWithApiType() {
        codegen.additionalProperties().put(CitrusJavaCodegen.API_TYPE, "XXX");

        assertThatThrownBy(() -> codegen.processOpts())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown API_TYPE: 'XXX'");
    }

    @Test
    void testProcessOptsValidApiType() {
        codegen.additionalProperties().put(CitrusJavaCodegen.API_TYPE, CitrusJavaCodegen.API_TYPE_REST);
        codegen.processOpts();

        assertThat(codegen.additionalProperties())
                .containsEntry(CitrusJavaCodegen.API_TYPE, CitrusJavaCodegen.API_TYPE_REST);
    }

    @Test
    void testPreprocessOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        Info info = new Info();
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("x-api-owner", "citrus-framework");
        extensions.put("x-api-version", "2.0.0");
        info.setExtensions(extensions);
        openAPI.setInfo(info);

        codegen.preprocessOpenAPI(openAPI);

        assertThat(codegen.additionalProperties())
                .containsEntry("x-api-owner", "citrus-framework")
                .containsEntry("x-api-version", "2.0.0")
                .containsEntry("infoExtensions", extensions);
    }

    @Test
    void testFromProperty() {
        // Mock schema
        Schema<?> schema = Mockito.mock(Schema.class);

        // Call fromProperty and verify conversion
        CodegenProperty codegenProperty = codegen.fromProperty("name", schema, true);
        assertThat(codegenProperty)
                .isInstanceOf(CodegenProperty.class)
                .hasFieldOrPropertyWithValue("name", "_name")
                .hasFieldOrPropertyWithValue("required", true);
    }

    @Test
    void testFromFormProperty() {
        Schema<?> schema = Mockito.mock(Schema.class);

        @SuppressWarnings("unchecked")
        Set<String> imports = Mockito.mock(Set.class);

        CodegenParameter codegenParameter = codegen.fromFormProperty("formParam", schema, imports);
        assertThat(codegenParameter)
                .isInstanceOf(CustomCodegenParameter.class)
                .hasFieldOrPropertyWithValue("paramName", "formParam");
    }

    @Test
    void testFromParameter() {
        Parameter parameter = Mockito.mock(Parameter.class);

        @SuppressWarnings("unchecked")
        Set<String> imports = Mockito.mock(Set.class);

        CodegenParameter codegenParameter = codegen.fromParameter(parameter, imports);
        assertThat(codegenParameter)
                .isInstanceOf(CustomCodegenParameter.class);
    }

    @Test
    void testFromOperation() {
        Operation operation = Mockito.mock(Operation.class);
        List<Server> servers = Collections.emptyList();

        CodegenOperation codegenOperation = codegen.fromOperation("/path", "GET", operation, servers);
        assertThat(codegenOperation)
                .isInstanceOf(CustomCodegenOperation.class)
                .hasFieldOrPropertyWithValue("httpMethod", "GET")
                .hasFieldOrPropertyWithValue("path", "/path");
    }

}
