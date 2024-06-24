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

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.OpenApiInteractionValidator.Builder;
import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.validation.OpenApiRequestValidator;
import org.citrusframework.openapi.validation.OpenApiResponseValidator;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
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

import static org.citrusframework.openapi.OpenApiSettings.isGenerateOptionalFieldsGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledlobally;
import static org.citrusframework.openapi.OpenApiSettings.isResponseValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isValidateOptionalFieldsGlobally;

/**
 * OpenApi specification resolves URL or local file resources to a specification document.
 * <p>
 * The OpenApiSpecification class is responsible for handling the loading and processing of OpenAPI
 * specification documents from various sources, such as URLs or local files. It supports the
 * extraction and usage of key information from these documents, facilitating the interaction with
 * OpenAPI-compliant APIs.
 * </p>
 * <p>
 * The class maintains a set of aliases derived from the OpenAPI document's information. These
 * aliases typically include the title of the API and its version, providing easy reference and
 * identification. For example, if the OpenAPI document's title is "Sample API" and its version is
 * "1.0", the aliases set will include "Sample API" and "Sample API/1.0".
 * </p>
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

    public static final String HTTPS = "https";
    public static final String HTTP = "http";

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

    private boolean generateOptionalFields = isGenerateOptionalFieldsGlobally();

    private boolean validateOptionalFields = isValidateOptionalFieldsGlobally();

    private boolean requestValidationEnabled = isRequestValidationEnabledlobally();

    private boolean responseValidationEnabled = isResponseValidationEnabledGlobally();

    private final Set<String> aliases = Collections.synchronizedSet(new HashSet<>());

    /**
     * Maps the identifier (id) of an operation to OperationPathAdapters. Two different keys may be used for each operation.
     * Refer to {@link org.citrusframework.openapi.OpenApiSpecification#storeOperationPathAdapter} for more details.
     */
    private final Map<String, OperationPathAdapter> operationIdToOperationPathAdapter = new ConcurrentHashMap<>();

    /**
     * Stores the unique identifier (uniqueId) of an operation, derived from its HTTP method and path.
     * This identifier can always be determined and is therefore safe to use, even for operations without
     * an optional operationId defined.
     */
    private final Map<OasOperation, String> operationToUniqueId = new ConcurrentHashMap<>();

    private OpenApiRequestValidator openApiRequestValidator;
    
    private OpenApiResponseValidator openApiResponseValidator;

    public static OpenApiSpecification from(String specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        specification.setSpecUrl(specUrl);

        return specification;
    }

    public static OpenApiSpecification from(URL specUrl) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc;
        OpenApiInteractionValidator validator;
        if (specUrl.getProtocol().startsWith(HTTPS)) {
            openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(specUrl);
            validator = new OpenApiInteractionValidator.Builder().withInlineApiSpecification(
                OpenApiResourceLoader.rawFromSecuredWebResource(specUrl)).build();
        } else {
            openApiDoc = OpenApiResourceLoader.fromWebResource(specUrl);
            validator = new OpenApiInteractionValidator.Builder().withInlineApiSpecification(
                OpenApiResourceLoader.rawFromWebResource(specUrl)).build();
        }

        specification.setSpecUrl(specUrl.toString());
        specification.initPathLookups();
        specification.setOpenApiDoc(openApiDoc);
        specification.setValidator(validator);
        specification.setRequestUrl(
            String.format("%s://%s%s%s", specUrl.getProtocol(), specUrl.getHost(),
                specUrl.getPort() > 0 ? ":" + specUrl.getPort() : "",
                OasModelHelper.getBasePath(openApiDoc)));

        return specification;
    }

    public static OpenApiSpecification from(Resource resource) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc = OpenApiResourceLoader.fromFile(resource);
        OpenApiInteractionValidator validator = new Builder().withInlineApiSpecification(
            OpenApiResourceLoader.rawFromFile(resource)).build();

        specification.setOpenApiDoc(openApiDoc);
        specification.setValidator(validator);

        String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
            .orElse(Collections.singletonList(HTTP))
            .stream()
            .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
            .findFirst()
            .orElse(HTTP);

        specification.setSpecUrl(resource.getLocation());
        specification.setRequestUrl(
            String.format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
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
                } else if (httpClient != null && context.getReferenceResolver()
                    .isResolvable(httpClient, HttpClient.class)) {
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
                    initApiDoc(
                        () -> OpenApiResourceLoader.fromSecuredWebResource(specWebResource));
                    setValidator(new OpenApiInteractionValidator.Builder().withInlineApiSpecification(
                        OpenApiResourceLoader.rawFromSecuredWebResource(specWebResource)).build());
                } else {
                    initApiDoc(() -> OpenApiResourceLoader.fromWebResource(specWebResource));
                    setValidator(new OpenApiInteractionValidator.Builder().withInlineApiSpecification(
                        OpenApiResourceLoader.rawFromWebResource(specWebResource)).build());
                }

                if (requestUrl == null) {
                    setRequestUrl(String.format("%s://%s%s%s", specWebResource.getProtocol(),
                        specWebResource.getHost(),
                        specWebResource.getPort() > 0 ? ":" + specWebResource.getPort() : "",
                        OasModelHelper.getBasePath(openApiDoc)));
                }

            } else {
                Resource resource = Resources.create(resolvedSpecUrl);
                initApiDoc(
                    () -> OpenApiResourceLoader.fromFile(resource));
                setValidator(new OpenApiInteractionValidator.Builder().withInlineApiSpecification(
                    OpenApiResourceLoader.rawFromFile(resource)).build());

                if (requestUrl == null) {
                    String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                        .orElse(Collections.singletonList(HTTP))
                        .stream()
                        .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
                        .findFirst()
                        .orElse(HTTP);

                    setRequestUrl(
                        String.format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                            OasModelHelper.getBasePath(openApiDoc)));
                }
            }
        }

        return openApiDoc;
    }

    // provided for testing
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

    private void setValidator(OpenApiInteractionValidator openApiInteractionValidator) {
        openApiRequestValidator = new OpenApiRequestValidator(openApiInteractionValidator);
        openApiRequestValidator.setEnabled(requestValidationEnabled);
        
        openApiResponseValidator = new OpenApiResponseValidator(openApiInteractionValidator);
        openApiRequestValidator.setEnabled(responseValidationEnabled);
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

            if (StringUtils.isEmpty(path)) {
                logger.warn("Skipping path item without path.");
                return;
            }

            for (Map.Entry<String, OasOperation> operationEntry : OasModelHelper.getOperationMap(
                oasPathItem).entrySet()) {
                storeOperationPathAdapter(operationEntry.getValue(), path);
            }
        });
    }

    /**
     * Stores an {@link OperationPathAdapter} in {@link org.citrusframework.openapi.OpenApiSpecification#operationIdToOperationPathAdapter}.
     * The adapter is stored using two keys: the operationId (optional) and the full path of the operation, including the method.
     * The full path is always determinable and thus can always be safely used.
     *
     * @param operation The {@link OperationPathAdapter} to store.
     * @param path The full path of the operation, including the method.
     */
    private void storeOperationPathAdapter(OasOperation operation, String path) {

        String basePath = OasModelHelper.getBasePath(openApiDoc);
        String fullOperationPath = StringUtils.appendSegmentToUrlPath(basePath, path);

        OperationPathAdapter operationPathAdapter = new OperationPathAdapter(path, rootContextPath,
            StringUtils.appendSegmentToUrlPath(rootContextPath, path),  operation);

        String uniqueOperationId = OpenApiUtils.createFullPathOperationIdentifier(fullOperationPath, operation);
        operationToUniqueId.put(operation, uniqueOperationId);

        operationIdToOperationPathAdapter.put(uniqueOperationId, operationPathAdapter);
        if (StringUtils.hasText(operation.operationId)) {
            operationIdToOperationPathAdapter.put(operation.operationId, operationPathAdapter);
        }
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

    public boolean isRequestValidationEnabled() {
        return requestValidationEnabled;
    }

    public void setRequestValidationEnabled(boolean enabled) {
        this.requestValidationEnabled = enabled;
        if (this.openApiRequestValidator != null) {
            this.openApiRequestValidator.setEnabled(enabled);
        }
    }

    public boolean isResponseValidationEnabled() {
        return responseValidationEnabled;
    }

    public void setResponseValidationEnabled(boolean enabled) {
        this.responseValidationEnabled = enabled;
        if (this.openApiResponseValidator != null) {
            this.openApiResponseValidator.setEnabled(enabled);
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
            return Collections.emptySet();
        }

        Info info = document.info;
        if (info == null) {
            return Collections.emptySet();
        }

        Set<String> set = new HashSet<>();
        if (StringUtils.hasText(info.title)) {
            set.add(info.title);

            if (StringUtils.hasText(info.version)) {
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

    public Optional<OpenApiRequestValidator> getRequestValidator() {
        return Optional.ofNullable(openApiRequestValidator);
    }

    public Optional<OpenApiResponseValidator> getResponseValidator() {
        return Optional.ofNullable(openApiResponseValidator);
    }

    public OpenApiSpecification withRootContext(String rootContextPath) {
        setRootContextPath(rootContextPath);
        return this;
    }

    public String getUniqueId(OasOperation oasOperation) {
        return operationToUniqueId.get(oasOperation);
    }

}
