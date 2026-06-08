package org.citrusframework.openapi.generator.rest.extpetstore;

import static org.citrusframework.util.SystemProvider.SYSTEM_PROVIDER;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.util.EnvUtils;

public class ExtPetStoreOpenApi {

    private static final String PROPERTY_KEY = "citrus.extpetstore.openapi";

    private static final String DEFAULT_LOCATION = "org/citrusframework/openapi/generator/rest/extpetstore/ExtPetStore_openApi.yaml";

    private static final String API_LOCATION = EnvUtils.getConfigurationProperty(
        SYSTEM_PROVIDER,
        PROPERTY_KEY,
        DEFAULT_LOCATION
    );

    public static final OpenApiSpecification extPetStoreSpecification = loadOpenApi();

    private static OpenApiSpecification loadOpenApi() {
        try {
            OpenApiSpecification openApiSpecification = OpenApiSpecification.from(API_LOCATION);

            // Make sure we are able to load it.
            openApiSpecification.getOpenApiDoc(new TestContext());

            return openApiSpecification;
        } catch (Exception e) {
            String msg = String.format("""
                Failed to load OpenAPI specification from '%s'.

                Possible reasons include:
                  • The generated resource file was not copied correctly during code generation.
                  • The resource folder containing the generated OpenAPI file is not on the classpath.
                  • The OpenAPI file exists but contains syntax or semantic errors that prevent parsing.

                By default, the code generator copies the OpenAPI definition into the generated sources
                under '%s'. Please verify that this file exists and is packaged with your compiled classes.

                If you need to override the location, you can specify it via:
                  • System property: -D%s=/path/to/openapi.yaml
                  • Environment variable: %s=/path/to/openapi.yaml

                """,
                API_LOCATION, DEFAULT_LOCATION, PROPERTY_KEY, EnvUtils.transformPropertyToEnv(PROPERTY_KEY)
            );
            throw new CitrusRuntimeException(msg, e);
        }
    }
}
