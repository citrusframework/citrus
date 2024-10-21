/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi;

import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.citrusframework.openapi.validation.OpenApiValidationContextLoader;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedSet;
import static org.citrusframework.openapi.OpenApiSettings.isGenerateOptionalFieldsGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isResponseValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isValidateOptionalFieldsGlobally;
import static org.citrusframework.util.StringUtils.appendSegmentToUrlPath;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;

/**
 * The OpenApiSpecification class is responsible for handling the loading and processing of OpenAPI
 * specification documents from various sources, such as URLs or local files. It supports the
 * extraction and usage of key information from these documents, facilitating the interaction with
 * OpenAPI-compliant APIs.
 * <p>
 * The class maintains a set of aliases derived from the OpenAPI document's information. These
 * aliases typically include the title of the API and its version, providing easy reference and
 * identification. For example, if the OpenAPI document's title is "Sample API" and its version is
 * "1.0", the aliases set will include "Sample API" and "Sample API/1.0".
 * <p>
 * Users are responsible for ensuring that the sources provided to this class have unique aliases,
 * or at least use the correct alias. If the same API is registered with different versions, all
 * versions will likely share the same title alias but can be distinguished by the version alias
 * (e.g., "Sample API/1.0" and "Sample API/2.0"). This distinction is crucial to avoid conflicts and
 * ensure the correct identification and reference of each OpenAPI specification. Also note, that
 * aliases may be added manually or programmatically by
 * {@link OpenApiSpecification#addAlias(String)}.
 */
public class OpenApiSpecification {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiSpecification.class);

    private static final String HTTPS = "https";
    private static final String HTTP = "http";

    private final Set<String> aliases = synchronizedSet(new HashSet<>());

    /**
     * Maps the identifier (id) of an operation to OperationPathAdapters. Two different keys may be
     * used for each operation. Refer to
     * {@link org.citrusframework.openapi.OpenApiSpecification#storeOperationPathAdapter} for more
     * details.
     */
    private final Map<String, OperationPathAdapter> operationIdToOperationPathAdapter = new ConcurrentHashMap<>();

    /**
     * Stores the unique identifier (uniqueId) of an operation, derived from its HTTP method and
     * path. This identifier can always be determined and is therefore safe to use, even for
     * operations without an optional operationId defined.
     */
    private final Map<OasOperation, String> operationToUniqueId = new ConcurrentHashMap<>();

    /**
     * URL to load the OpenAPI specification
     */
    private String specUrl;
    private String httpClient;
    private String requestUrl;

    /**
     * The optional root context path to which the OpenAPI is hooked. This path is prepended to the
     * base path specified in the OpenAPI configuration. If no root context path is specified, only
     * the base path and additional segments are used.
     */
    private String rootContextPath;
    private OasDocument openApiDoc;
    private OpenApiValidationContext openApiValidationContext;
    private boolean generateOptionalFields = isGenerateOptionalFieldsGlobally();
    private boolean validateOptionalFields = isValidateOptionalFieldsGlobally();

    /**
     * Flag to indicate, whether request validation is enabled on api level. Api level overrules global
     * level and may be overruled by request level.
     */

    private boolean apiRequestValidationEnabled = isRequestValidationEnabledGlobally();

    /**
     * Flag to indicate, whether response validation is enabled on api level. Api level overrules global
     * level and may be overruled by request level.
     */
    private boolean apiResponseValidationEnabled = isResponseValidationEnabledGlobally();

    public static OpenApiSpecification from(String specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        specification.setSpecUrl(specUrl);

        return specification;
    }

    public static OpenApiSpecification from(URL specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc;
        OpenApiValidationContext openApiValidationContext;
        if (specUrl.getProtocol().startsWith(HTTPS)) {
            openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(specUrl);
            openApiValidationContext = OpenApiValidationContextLoader.fromSecuredWebResource(specUrl);
        } else {
            openApiDoc = OpenApiResourceLoader.fromWebResource(specUrl);
            openApiValidationContext = OpenApiValidationContextLoader.fromWebResource(specUrl);
        }

        specification.setSpecUrl(specUrl.toString());
        specification.initPathLookups();
        specification.setOpenApiDoc(openApiDoc);
        specification.setOpenApiValidationContext(openApiValidationContext);
        specification.setRequestUrl(
                format("%s://%s%s%s", specUrl.getProtocol(), specUrl.getHost(), specUrl.getPort() > 0 ? ":" + specUrl.getPort() : "",
                        OasModelHelper.getBasePath(openApiDoc)));

        return specification;
    }

    public static OpenApiSpecification from(Resource resource) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc = OpenApiResourceLoader.fromFile(resource);

        specification.setOpenApiDoc(openApiDoc);
        specification.setOpenApiValidationContext(OpenApiValidationContextLoader.fromFile(resource));

        String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                .orElse(singletonList(HTTP))
                .stream()
                .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
                .findFirst()
                .orElse(HTTP);

        specification.setSpecUrl(resource.getLocation());
        specification.setRequestUrl(format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                OasModelHelper.getBasePath(openApiDoc)));

        return specification;
    }

    public synchronized OasDocument getOpenApiDoc(TestContext context) {
        if (openApiDoc != null) {
            return openApiDoc;
        }

        if (specUrl != null) {
            String resolvedSpecUrl = context.replaceDynamicContentInString(specUrl);

            if (resolvedSpecUrl.startsWith("/")) {
                // relative path URL - try to resolve with given request URL
                if (requestUrl != null) {
                    resolvedSpecUrl =
                            requestUrl.endsWith("/") ? requestUrl + resolvedSpecUrl.substring(1)
                                    : requestUrl + resolvedSpecUrl;
                } else if (httpClient != null && context.getReferenceResolver().isResolvable(httpClient, HttpClient.class)) {
                    String baseUrl = context.getReferenceResolver()
                            .resolve(httpClient, HttpClient.class).getEndpointConfiguration()
                            .getRequestUrl();
                    resolvedSpecUrl = baseUrl.endsWith("/") ? baseUrl + resolvedSpecUrl.substring(1)
                            : baseUrl + resolvedSpecUrl;
                } else {
                    throw new CitrusRuntimeException(
                            ("Failed to resolve OpenAPI spec URL from relative path %s - " +
                                    "make sure to provide a proper base URL when using relative paths").formatted(
                                    resolvedSpecUrl));
                }
            }

            if (resolvedSpecUrl.startsWith(HTTP)) {
                URL specWebResource = toSpecUrl(resolvedSpecUrl);
                if (resolvedSpecUrl.startsWith(HTTPS)) {
                    initApiDoc(() -> OpenApiResourceLoader.fromSecuredWebResource(specWebResource));
                    setOpenApiValidationContext(OpenApiValidationContextLoader.fromSecuredWebResource(specWebResource));
                } else {
                    initApiDoc(() -> OpenApiResourceLoader.fromWebResource(specWebResource));
                    setOpenApiValidationContext(OpenApiValidationContextLoader.fromWebResource(specWebResource));
                }

                if (requestUrl == null) {
                    setRequestUrl(format("%s://%s%s%s", specWebResource.getProtocol(),
                            specWebResource.getHost(),
                            specWebResource.getPort() > 0 ? ":" + specWebResource.getPort() : "",
                            OasModelHelper.getBasePath(openApiDoc)));
                }

            } else {
                Resource resource = Resources.create(resolvedSpecUrl);
                initApiDoc(() -> OpenApiResourceLoader.fromFile(resource));
                setOpenApiValidationContext(OpenApiValidationContextLoader.fromFile(resource));

                if (requestUrl == null) {
                    String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                            .orElse(singletonList(HTTP))
                            .stream()
                            .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
                            .findFirst()
                            .orElse(HTTP);

                    setRequestUrl(
                            format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                                    OasModelHelper.getBasePath(openApiDoc)));
                }
            }
        }

        return openApiDoc;
    }

    public OpenApiValidationContext getOpenApiValidationContext() {
        return openApiValidationContext;
    }

    private void setOpenApiValidationContext(OpenApiValidationContext openApiValidationContext) {
        this.openApiValidationContext = openApiValidationContext;
        this.openApiValidationContext.setResponseValidationEnabled(apiResponseValidationEnabled);
        this.openApiValidationContext.setRequestValidationEnabled(apiRequestValidationEnabled);
    }

    // exposed for testing
    URL toSpecUrl(String resolvedSpecUrl) {
        try {
            return URI.create(resolvedSpecUrl).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(
                    "Failed to retrieve Open API specification as web resource: " + resolvedSpecUrl, e);
        }
    }

    void setOpenApiDoc(OasDocument openApiDoc) {
        initApiDoc(() -> openApiDoc);
    }

    private void initApiDoc(Supplier<OasDocument> openApiDocSupplier) {
        this.openApiDoc = openApiDocSupplier.get();
        this.aliases.addAll(collectAliases(openApiDoc));
        initPathLookups();
    }

    private void initPathLookups() {
        if (this.openApiDoc == null) {
            return;
        }

        operationIdToOperationPathAdapter.clear();
        OasModelHelper.visitOasOperations(this.openApiDoc, (oasPathItem, oasOperation) -> {
            String path = oasPathItem.getPath();

            if (isEmpty(path)) {
                logger.warn("Skipping path item without path.");
                return;
            }

            for (Map.Entry<String, OasOperation> operationEntry : OasModelHelper.getOperationMap(oasPathItem).entrySet()) {
                storeOperationPathAdapter(operationEntry.getValue(), path);
            }
        });
    }

    /**
     * Stores an {@link OperationPathAdapter} in
     * {@link org.citrusframework.openapi.OpenApiSpecification#operationIdToOperationPathAdapter}.
     * The adapter is stored using two keys: the operationId (optional) and the full path of the
     * operation, including the method. The full path is always determinable and thus can always be
     * safely used.
     *
     * @param operation The {@link OperationPathAdapter} to store.
     * @param path      The full path of the operation, including the method.
     */
    private void storeOperationPathAdapter(OasOperation operation, String path) {
        String basePath = OasModelHelper.getBasePath(openApiDoc);
        String fullOperationPath = appendSegmentToUrlPath(basePath, path);

        String uniqueOperationId = OpenApiUtils.createFullPathOperationIdentifier(operation, fullOperationPath);
        operationToUniqueId.put(operation, uniqueOperationId);

        OperationPathAdapter operationPathAdapter = new OperationPathAdapter(path, rootContextPath, appendSegmentToUrlPath(rootContextPath, path), operation, uniqueOperationId);

        operationIdToOperationPathAdapter.put(uniqueOperationId, operationPathAdapter);
        if (hasText(operation.operationId)) {
            operationIdToOperationPathAdapter.put(operation.operationId, operationPathAdapter);
        }
    }

    public String getSpecUrl() {
        return specUrl;
    }

    public void setSpecUrl(String specUrl) {
        this.specUrl = specUrl;
    }

    public String getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(String httpClient) {
        this.httpClient = httpClient;
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

    public boolean isApiRequestValidationEnabled() {
        return apiRequestValidationEnabled;
    }

    public void setApiRequestValidationEnabled(boolean enabled) {
        this.apiRequestValidationEnabled = enabled;
        if (this.openApiValidationContext != null) {
            this.openApiValidationContext.setRequestValidationEnabled(enabled);
        }
    }

    public boolean isApiResponseValidationEnabled() {
        return apiResponseValidationEnabled;
    }

    public void setApiResponseValidationEnabled(boolean enabled) {
        this.apiResponseValidationEnabled = enabled;
        if (this.openApiValidationContext != null) {
            this.openApiValidationContext.setResponseValidationEnabled(enabled);
        }
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

    public String getRootContextPath() {
        return rootContextPath;
    }

    public void setRootContextPath(String rootContextPath) {
        this.rootContextPath = rootContextPath;
        initPathLookups();
    }

    public void addAlias(String alias) {
        aliases.add(alias);
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    private Collection<String> collectAliases(OasDocument document) {
        if (document == null) {
            return emptySet();
        }

        Info info = document.info;
        if (info == null) {
            return emptySet();
        }

        Set<String> set = new HashSet<>();
        if (hasText(info.title)) {
            set.add(info.title);

            if (hasText(info.version)) {
                set.add(info.title + "/" + info.version);
            }
        }

        return set;
    }

    public Optional<OperationPathAdapter> getOperation(String operationId, TestContext context) {
        if (operationId == null) {
            return Optional.empty();
        }

        // This is ugly, but we need not make sure that the openApiDoc is initialized, which might
        // happen, when instance is created with org.citrusframework.openapi.OpenApiSpecification.from(java.lang.String)
        if (openApiDoc == null) {
            getOpenApiDoc(context);
        }

        return Optional.ofNullable(operationIdToOperationPathAdapter.get(operationId));
    }

    public OpenApiSpecification withRootContext(String rootContextPath) {
        setRootContextPath(rootContextPath);
        return this;
    }

    public String getUniqueId(OasOperation oasOperation) {
        return operationToUniqueId.get(oasOperation);
    }
}
