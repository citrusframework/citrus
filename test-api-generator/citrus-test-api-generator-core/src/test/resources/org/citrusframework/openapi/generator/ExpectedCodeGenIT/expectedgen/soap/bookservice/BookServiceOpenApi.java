package org.citrusframework.openapi.generator.soap.bookservice;

import org.citrusframework.openapi.OpenApiSpecification;

public class BookServiceOpenApi {

    public static final OpenApiSpecification bookServiceSpecification = OpenApiSpecification
        .from(BookServiceOpenApi.class.getResource("BookService_openApi.yaml"));

}
