package org.citrusframework.openapi.generator.rest.petstore;

import org.citrusframework.openapi.OpenApiSpecification;

public class PetStoreOpenApi {

    public static final OpenApiSpecification petStoreSpecification = OpenApiSpecification
        .from(PetStoreOpenApi.class.getResource("petStore_openApi.yaml"));

}
