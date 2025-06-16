package org.citrusframework.openapi.generator.rest.extpetstore;

import org.citrusframework.openapi.OpenApiSpecification;

public class ExtPetStoreOpenApi {

    public static final OpenApiSpecification extPetStoreSpecification = OpenApiSpecification
        .from(ExtPetStoreOpenApi.class.getResource("ExtPetStore_openApi.yaml"));

}
