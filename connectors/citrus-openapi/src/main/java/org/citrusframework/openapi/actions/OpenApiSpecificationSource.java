package org.citrusframework.openapi.actions;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.spi.ReferenceResolver;

import java.util.Objects;

import static org.citrusframework.util.StringUtils.isEmpty;

/**
 * The {@code OpenApiSpecificationSource} class is responsible for managing and resolving an
 * {@link OpenApiSpecification} instance. It can either directly contain an
 * {@link OpenApiSpecification} or reference one by an alias.
 */
public class OpenApiSpecificationSource {

    private OpenApiSpecification openApiSpecification;

    private String openApiAlias;

    private String httpClient;

    public OpenApiSpecificationSource(OpenApiSpecification openApiSpecification) {
        this.openApiSpecification = openApiSpecification;
    }

    public OpenApiSpecificationSource(String openApiAlias) {
        this.openApiAlias = openApiAlias;
    }

    public OpenApiSpecification resolve(ReferenceResolver resolver) {
        if (openApiSpecification == null) {

            if (!isEmpty(openApiAlias)) {
                openApiSpecification = resolver.resolveAll(OpenApiRepository.class).values()
                        .stream()
                        .map(openApiRepository -> openApiRepository.openApi(openApiAlias)).
                        filter(Objects::nonNull).
                        findFirst()
                        .orElseThrow(() ->
                                new CitrusRuntimeException(
                                        "Unable to resolve OpenApiSpecification from alias '%s'. Known aliases for open api specs are '%s'".formatted(
                                                openApiAlias, OpenApiUtils.getKnownOpenApiAliases(resolver)))
                        );
            } else {
                throw new CitrusRuntimeException(
                        "Unable to resolve OpenApiSpecification. Neither OpenAPI spec, nor OpenAPI  alias are specified.");
            }
        }

        if (httpClient != null) {
            openApiSpecification.setHttpClient(httpClient);
        }

        return openApiSpecification;
    }

    public void setHttpClient(String httpClient) {
        this.httpClient = httpClient;
    }
}
