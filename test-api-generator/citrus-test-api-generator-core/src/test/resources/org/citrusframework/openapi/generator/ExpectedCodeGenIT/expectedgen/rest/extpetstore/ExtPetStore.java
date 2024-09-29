package org.citrusframework.openapi.generator.rest.extpetstore;

import java.net.URL;

public class ExtPetStore {

    public static URL extPetStoreApi() {
        return ExtPetStore.class.getResource("ExtPetStore_openApi.yaml");
    }

}
