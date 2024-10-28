package org.citrusframework.openapi.generator.soap.bookservice;

import java.net.URL;

public class BookService {

    public static URL bookServiceApi() {
        return BookService.class.getResource("BookService_openApi.yaml");
    }
}
