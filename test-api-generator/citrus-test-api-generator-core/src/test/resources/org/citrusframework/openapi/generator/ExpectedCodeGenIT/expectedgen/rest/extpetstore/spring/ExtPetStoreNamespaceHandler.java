package org.citrusframework.openapi.generator.rest.extpetstore.spring;

import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.testapi.RestApiSendMessageActionBuilder;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.generator.rest.extpetstore.request.ExtPetApi;
import org.citrusframework.openapi.testapi.spring.RestApiReceiveMessageActionParser;
import org.citrusframework.openapi.testapi.spring.RestApiSendMessageActionParser;
import org.citrusframework.openapi.generator.rest.extpetstore.ExtPetStore;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.CitrusJavaCodegen", date = "2024-10-05T19:07:46.194751400+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class ExtPetStoreNamespaceHandler extends NamespaceHandlerSupport {

    private final OpenApiSpecification openApiSpecification = OpenApiSpecification.from(
        ExtPetStore.extPetStoreApi());

    @Override
    public void init() {

            registerOperationParsers(ExtPetApi.class,"generate-vaccination-report", "generateVaccinationReport", "/pet/vaccination/status-report",
                ExtPetApi.GenerateVaccinationReportSendActionBuilder.class,
                ExtPetApi.GenerateVaccinationReportReceiveActionBuilder.class,
                new String[]{ "template", "reqIntVal" },
            new String[]{ "optIntVal", "optBoolVal", "optNumberVal", "optStringVal", "optDateVal", "additionalData", "schema" });

            registerOperationParsers(ExtPetApi.class,"get-pet-by-id-with-api-key-authentication", "getPetByIdWithApiKeyAuthentication", "/secure-api-key/pet/{petId}",
                ExtPetApi.GetPetByIdWithApiKeyAuthenticationSendActionBuilder.class,
                ExtPetApi.GetPetByIdWithApiKeyAuthenticationReceiveActionBuilder.class,
                new String[]{ "petId", "allDetails" },
            new String[]{ "details", "requesterInformation", "apiKeyQuery", "apiKeyHeader", "apiKeyCookie" });

            registerOperationParsers(ExtPetApi.class,"get-pet-by-id-with-basic-authentication", "getPetByIdWithBasicAuthentication", "/secure-basic/pet/{petId}",
                ExtPetApi.GetPetByIdWithBasicAuthenticationSendActionBuilder.class,
                ExtPetApi.GetPetByIdWithBasicAuthenticationReceiveActionBuilder.class,
                new String[]{ "petId", "allDetails" },
            new String[]{ "details", "requesterInformation", "basicAuthUsername", "basicAuthPassword" });

            registerOperationParsers(ExtPetApi.class,"get-pet-by-id-with-bearer-authentication", "getPetByIdWithBearerAuthentication", "/secure-bearer/pet/{petId}",
                ExtPetApi.GetPetByIdWithBearerAuthenticationSendActionBuilder.class,
                ExtPetApi.GetPetByIdWithBearerAuthenticationReceiveActionBuilder.class,
                new String[]{ "petId", "allDetails" },
            new String[]{ "details", "requesterInformation", "basicAuthBearer" });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-cookie", "getPetWithCookie", "/pet/{petId}",
                ExtPetApi.GetPetWithCookieSendActionBuilder.class,
                ExtPetApi.GetPetWithCookieReceiveActionBuilder.class,
                new String[]{ "petId", "sessionId" },
            new String[]{ "optTrxId" });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-deep-object-type-query", "getPetWithDeepObjectTypeQuery", "/pet/query/deep/object",
                ExtPetApi.GetPetWithDeepObjectTypeQuerySendActionBuilder.class,
                ExtPetApi.GetPetWithDeepObjectTypeQueryReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-exploded-style-cookie", "getPetWithFormExplodedStyleCookie", "/pet/cookie/form/exploded",
                ExtPetApi.GetPetWithFormExplodedStyleCookieSendActionBuilder.class,
                ExtPetApi.GetPetWithFormExplodedStyleCookieReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-object-style-cookie", "getPetWithFormObjectStyleCookie", "/pet/cookie/form/object",
                ExtPetApi.GetPetWithFormObjectStyleCookieSendActionBuilder.class,
                ExtPetApi.GetPetWithFormObjectStyleCookieReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-style-cookie", "getPetWithFormStyleCookie", "/pet/cookie/form",
                ExtPetApi.GetPetWithFormStyleCookieSendActionBuilder.class,
                ExtPetApi.GetPetWithFormStyleCookieReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-style-exploded-object-query", "getPetWithFormStyleExplodedObjectQuery", "/pet/query/form/exploded/object",
                ExtPetApi.GetPetWithFormStyleExplodedObjectQuerySendActionBuilder.class,
                ExtPetApi.GetPetWithFormStyleExplodedObjectQueryReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-style-exploded-query", "getPetWithFormStyleExplodedQuery", "/pet/query/form/exploded",
                ExtPetApi.GetPetWithFormStyleExplodedQuerySendActionBuilder.class,
                ExtPetApi.GetPetWithFormStyleExplodedQueryReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-style-object-query", "getPetWithFormStyleObjectQuery", "/pet/query/form/object",
                ExtPetApi.GetPetWithFormStyleObjectQuerySendActionBuilder.class,
                ExtPetApi.GetPetWithFormStyleObjectQueryReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-form-style-query", "getPetWithFormStyleQuery", "/pet/query/form",
                ExtPetApi.GetPetWithFormStyleQuerySendActionBuilder.class,
                ExtPetApi.GetPetWithFormStyleQueryReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-label-style-array", "getPetWithLabelStyleArray", "/pet/label/{petId}",
                ExtPetApi.GetPetWithLabelStyleArraySendActionBuilder.class,
                ExtPetApi.GetPetWithLabelStyleArrayReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-label-style-array-exploded", "getPetWithLabelStyleArrayExploded", "/pet/label/exploded/{petId}",
                ExtPetApi.GetPetWithLabelStyleArrayExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithLabelStyleArrayExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-label-style-object", "getPetWithLabelStyleObject", "/pet/label/object/{petId}",
                ExtPetApi.GetPetWithLabelStyleObjectSendActionBuilder.class,
                ExtPetApi.GetPetWithLabelStyleObjectReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-label-style-object-exploded", "getPetWithLabelStyleObjectExploded", "/pet/label/exploded/object/{petId}",
                ExtPetApi.GetPetWithLabelStyleObjectExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithLabelStyleObjectExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-matrix-style-array", "getPetWithMatrixStyleArray", "/pet/matrix/{petId}",
                ExtPetApi.GetPetWithMatrixStyleArraySendActionBuilder.class,
                ExtPetApi.GetPetWithMatrixStyleArrayReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-matrix-style-array-exploded", "getPetWithMatrixStyleArrayExploded", "/pet/matrix/exploded/{petId}",
                ExtPetApi.GetPetWithMatrixStyleArrayExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithMatrixStyleArrayExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-matrix-style-object", "getPetWithMatrixStyleObject", "/pet/matrix/object/{petId}",
                ExtPetApi.GetPetWithMatrixStyleObjectSendActionBuilder.class,
                ExtPetApi.GetPetWithMatrixStyleObjectReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-matrix-style-object-exploded", "getPetWithMatrixStyleObjectExploded", "/pet/matrix/exploded/object/{petId}",
                ExtPetApi.GetPetWithMatrixStyleObjectExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithMatrixStyleObjectExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-array", "getPetWithSimpleStyleArray", "/pet/simple/{petId}",
                ExtPetApi.GetPetWithSimpleStyleArraySendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleArrayReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-array-exploded", "getPetWithSimpleStyleArrayExploded", "/pet/simple/exploded/{petId}",
                ExtPetApi.GetPetWithSimpleStyleArrayExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleArrayExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-exploded-header", "getPetWithSimpleStyleExplodedHeader", "/pet/header/simple/exploded",
                ExtPetApi.GetPetWithSimpleStyleExplodedHeaderSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleExplodedHeaderReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-exploded-object-header", "getPetWithSimpleStyleExplodedObjectHeader", "/pet/header/simple/exploded/object",
                ExtPetApi.GetPetWithSimpleStyleExplodedObjectHeaderSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleExplodedObjectHeaderReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-header", "getPetWithSimpleStyleHeader", "/pet/header/simple",
                ExtPetApi.GetPetWithSimpleStyleHeaderSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleHeaderReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-object", "getPetWithSimpleStyleObject", "/pet/simple/object/{petId}",
                ExtPetApi.GetPetWithSimpleStyleObjectSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleObjectReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-object-exploded", "getPetWithSimpleStyleObjectExploded", "/pet/simple/exploded/object/{petId}",
                ExtPetApi.GetPetWithSimpleStyleObjectExplodedSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleObjectExplodedReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"get-pet-with-simple-style-object-header", "getPetWithSimpleStyleObjectHeader", "/pet/header/simple/object",
                ExtPetApi.GetPetWithSimpleStyleObjectHeaderSendActionBuilder.class,
                ExtPetApi.GetPetWithSimpleStyleObjectHeaderReceiveActionBuilder.class,
                new String[]{ "petId" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"post-vaccination-document", "postVaccinationDocument", "/pet/vaccination/{bucket}/{filename}",
                ExtPetApi.PostVaccinationDocumentSendActionBuilder.class,
                ExtPetApi.PostVaccinationDocumentReceiveActionBuilder.class,
                new String[]{ "bucket", "filename" },
            new String[]{  });

            registerOperationParsers(ExtPetApi.class,"post-vaccination-form-data", "postVaccinationFormData", "/pet/vaccination/form",
                ExtPetApi.PostVaccinationFormDataSendActionBuilder.class,
                ExtPetApi.PostVaccinationFormDataReceiveActionBuilder.class,
                new String[]{  },
            new String[]{ "vaccine", "isFirstVaccination", "doseNumber", "vaccinationDate" });

            registerOperationParsers(ExtPetApi.class,"update-pet-with-array-query-data", "updatePetWithArrayQueryData", "/pet/{petId}",
                ExtPetApi.UpdatePetWithArrayQueryDataSendActionBuilder.class,
                ExtPetApi.UpdatePetWithArrayQueryDataReceiveActionBuilder.class,
                new String[]{ "petId", "_name", "status", "tags", "nicknames", "sampleStringHeader" },
            new String[]{ "sampleIntHeader" });

            registerOperationParsers(ExtPetApi.class,"update-pet-with-form-url-encoded", "updatePetWithFormUrlEncoded", "/pet/form/{petId}",
                ExtPetApi.UpdatePetWithFormUrlEncodedSendActionBuilder.class,
                ExtPetApi.UpdatePetWithFormUrlEncodedReceiveActionBuilder.class,
                new String[]{ "petId", "_name", "status", "age", "tags" },
            new String[]{ "owners", "nicknames" });
    }

    private void registerOperationParsers(Class<? extends GeneratedApi> apiClass, String elementName, String operationName, String path,
        Class<? extends RestApiSendMessageActionBuilder> sendBeanClass,
        Class<? extends RestApiReceiveMessageActionBuilder> receiveBeanClass,
        String[] constructorParameters,
        String[] nonConstructorParameters) {

        RestApiSendMessageActionParser sendParser = new RestApiSendMessageActionParser(openApiSpecification, operationName,
            path,
            apiClass,
            sendBeanClass,
            receiveBeanClass,
            "extpetstore.endpoint");
        sendParser.setConstructorParameters(constructorParameters);
        sendParser.setNonConstructorParameters(nonConstructorParameters);
        registerBeanDefinitionParser("send-"+elementName, sendParser);

        RestApiReceiveMessageActionParser receiveParser = new RestApiReceiveMessageActionParser(openApiSpecification,
        operationName, apiClass, receiveBeanClass, "extpetstore.endpoint");
        registerBeanDefinitionParser("receive-"+elementName, receiveParser);
    }

}
