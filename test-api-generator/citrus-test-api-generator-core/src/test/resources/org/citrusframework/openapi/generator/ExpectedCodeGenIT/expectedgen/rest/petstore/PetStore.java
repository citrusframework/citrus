package org.citrusframework.openapi.generator.rest.petstore;

import java.net.URL;

public class PetStore {

    public static URL petStoreApi() {
        return PetStore.class.getResource("petStore_openApi.yaml");
    }

}
