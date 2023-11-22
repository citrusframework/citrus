/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.openapi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import io.apicurio.datamodels.openapi.models.OasDocument;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

/**
 * OpenApi specification resolves URL or local file resources to a specification document.
 */
public class OpenApiSpecification {

    /** URL to load the OpenAPI specification */
    private String specUrl;

    private String httpClient;
    private String requestUrl;

    private OasDocument openApiDoc;

    private boolean generateOptionalFields = true;

    private boolean validateOptionalFields = true;

    public static OpenApiSpecification from(String specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        specification.setSpecUrl(specUrl);

        return specification;
    }

    public static OpenApiSpecification from(URL specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc;
        if (specUrl.getProtocol().startsWith("https")) {
            openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(specUrl);
        } else {
            openApiDoc = OpenApiResourceLoader.fromWebResource(specUrl);
        }

        specification.setSpecUrl(specUrl.toString());
        specification.setOpenApiDoc(openApiDoc);
        specification.setRequestUrl(String.format("%s://%s%s%s", specUrl.getProtocol(), specUrl.getHost(), specUrl.getPort() > 0 ? ":" + specUrl.getPort() : "", OasModelHelper.getBasePath(openApiDoc)));

        return specification;
    }

    public static OpenApiSpecification from(Resource resource) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc = OpenApiResourceLoader.fromFile(resource);

        specification.setOpenApiDoc(openApiDoc);

        String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                .orElse(Collections.singletonList("http"))
                .stream()
                .filter(s -> s.equals("http") || s.equals("https"))
                .findFirst()
                .orElse("http");

        specification.setSpecUrl(resource.getLocation());
        specification.setRequestUrl(String.format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc), OasModelHelper.getBasePath(openApiDoc)));

        return specification;
    }

    public OasDocument getOpenApiDoc(TestContext context) {
        if (openApiDoc != null) {
            return openApiDoc;
        }

        if (specUrl != null) {
            String resolvedSpecUrl = context.replaceDynamicContentInString(specUrl);

            if (resolvedSpecUrl.startsWith("/")) {
                // relative path URL - try to resolve with given request URL
                if (requestUrl != null) {
                    resolvedSpecUrl = requestUrl.endsWith("/") ? requestUrl + resolvedSpecUrl.substring(1) : requestUrl + resolvedSpecUrl;
                } else if (httpClient != null && context.getReferenceResolver().isResolvable(httpClient, HttpClient.class)) {
                    String baseUrl = context.getReferenceResolver().resolve(httpClient, HttpClient.class).getEndpointConfiguration().getRequestUrl();
                    resolvedSpecUrl = baseUrl.endsWith("/") ? baseUrl + resolvedSpecUrl.substring(1) : baseUrl + resolvedSpecUrl;;
                } else {
                    throw new CitrusRuntimeException(("Failed to resolve OpenAPI spec URL from relative path %s - " +
                            "make sure to provide a proper base URL when using relative paths").formatted(resolvedSpecUrl));
                }
            }

            if (resolvedSpecUrl.startsWith("http")) {
                try {
                    URL specWebResource = new URL(resolvedSpecUrl);
                    if (resolvedSpecUrl.startsWith("https")) {
                        openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(specWebResource);
                    } else {
                        openApiDoc = OpenApiResourceLoader.fromWebResource(specWebResource);
                    }

                    if (requestUrl == null) {
                        setRequestUrl(String.format("%s://%s%s%s", specWebResource.getProtocol(), specWebResource.getHost(), specWebResource.getPort() > 0 ? ":" + specWebResource.getPort() : "", OasModelHelper.getBasePath(openApiDoc)));
                    }
                } catch (MalformedURLException e) {
                    throw new IllegalStateException("Failed to retrieve Open API specification as web resource: " + specUrl, e);
                }
            } else {
                openApiDoc = OpenApiResourceLoader.fromFile(Resources.create(resolvedSpecUrl));

                if (requestUrl == null) {
                    String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                            .orElse(Collections.singletonList("http"))
                            .stream()
                            .filter(s -> s.equals("http") || s.equals("https"))
                            .findFirst()
                            .orElse("http");

                    setRequestUrl(String.format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc), OasModelHelper.getBasePath(openApiDoc)));
                }
            }
        }

        return openApiDoc;
    }

    public void setOpenApiDoc(OasDocument openApiDoc) {
        this.openApiDoc = openApiDoc;
    }

    public String getSpecUrl() {
        return specUrl;
    }

    public void setSpecUrl(String specUrl) {
        this.specUrl = specUrl;
    }

    public void setHttpClient(String httpClient) {
        this.httpClient = httpClient;
    }

    public String getHttpClient() {
        return httpClient;
    }

    public String getRequestUrl() {
        if (requestUrl == null) {
            return specUrl;
        }

        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public boolean isGenerateOptionalFields() {
        return generateOptionalFields;
    }

    public void setGenerateOptionalFields(boolean generateOptionalFields) {
        this.generateOptionalFields = generateOptionalFields;
    }

    public boolean isValidateOptionalFields() {
        return validateOptionalFields;
    }

    public void setValidateOptionalFields(boolean validateOptionalFields) {
        this.validateOptionalFields = validateOptionalFields;
    }
}
