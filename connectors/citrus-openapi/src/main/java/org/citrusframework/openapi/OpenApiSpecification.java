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

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedSet;
import static org.citrusframework.openapi.OpenApiSettings.isGenerateOptionalFieldsGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isNeglectBasePathGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledGlobally;
import static org.citrusframework.openapi.OpenApiSettings.isResponseValidationEnabledGlobally;
import static org.citrusframework.openapi.model.OasModelHelper.getBasePath;
import static org.citrusframework.util.StringUtils.appendSegmentToUrlPath;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;

import io.apicurio.datamodels.core.models.common.Info;
import io.apicurio.datamodels.openapi.models.OasDocument;
import io.apicurio.datamodels.openapi.models.OasOperation;
import io.apicurio.datamodels.openapi.models.OasPathItem;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.openapi.model.OasModelHelper;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.openapi.util.OpenApiUtils;
import org.citrusframework.openapi.validation.OpenApiValidationContext;
import org.citrusframework.openapi.validation.OpenApiValidationContextLoader;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * A unique identifier (UID) for this specification at runtime. The UID is generated based on the SHA
     * of the OpenAPI document combined with the full context path to which the API is attached.
     *
     * @see OpenApiSpecification#determineUid for detailed information on how the UID is generated.
     */
    private String uid;

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
     * Flag indicating whether the base path should be excluded when constructing the complete
     * operation path.
     * <p>
     * If set to {@code true}, the base path will be omitted from the final URL path construction.
     * This allows for more flexible path handling where the base path is not required.
     * </p>
     *
     * @see #getFullPath(OasPathItem) for the method affected by this flag
     */
    private boolean neglectBasePath = isNeglectBasePathGlobally();

    /**
     * The optional root context path to which the OpenAPI is hooked.
     * <p>
     * This path is prepended to the base path specified in the OpenAPI configuration. If no root
     * context path is specified, only the base path and additional segments are used when
     * constructing the complete URL path.
     * </p>
     *
     * @see #neglectBasePath for information on excluding the base path
     * @see #getFullPath(OasPathItem) for how this path is used in constructing the full operation
     * path
     */
    private String rootContextPath;

    private OasDocument openApiDoc;
    private OpenApiValidationContext openApiValidationContext;
    private boolean generateOptionalFields = isGenerateOptionalFieldsGlobally();

    /**
     * Flag to indicate, whether request validation is enabled on api level. Api level overrules
     * global level and may be overruled by request level.
     */

    private boolean apiRequestValidationEnabled = isRequestValidationEnabledGlobally();

    /**
     * Flag to indicate, whether response validation is enabled on api level. Api level overrules
     * global level and may be overruled by request level.
     */
    private boolean apiResponseValidationEnabled = isResponseValidationEnabledGlobally();

    /**
     * The policy that determines how OpenAPI validation errors are handled.
     */
    private final OpenApiValidationPolicy openApiValidationPolicy;

    public OpenApiSpecification() {
        this(OpenApiSettings.getOpenApiValidationPolicy());
    }

    public OpenApiSpecification(OpenApiValidationPolicy openApiValidationPolicy) {
        this.openApiValidationPolicy = openApiValidationPolicy;
    }

    /**
     * Creates an OpenAPI specification instance from the given URL applying the default validation policy.
     *
     * @param specUrl the URL pointing to the OpenAPI specification to load
     * @return an OpenApiSpecification instance populated with the document and validation context
     */

    public static OpenApiSpecification from(String specUrl) {
        return from(specUrl, OpenApiSettings.getOpenApiValidationPolicy());
    }

    /**
     * Creates an OpenAPI specification instance from the given url string.
     *
     * @param specUrl                 the URL pointing to the OpenAPI specification to load
     * @param openApiValidationPolicy the validation policy to apply to the loaded OpenApi
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification from(String specUrl,
        OpenApiValidationPolicy openApiValidationPolicy) {
        OpenApiSpecification specification = new OpenApiSpecification(openApiValidationPolicy);
        specification.setSpecUrl(specUrl);

        return specification;
    }

    /**
     * Creates an OpenAPI specification instance from the given URL applying the default validation policy.
     *
     * @param specUrl                 the URL pointing to the OpenAPI specification to load
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification from(URL specUrl) {
        return from(specUrl, OpenApiSettings.getOpenApiValidationPolicy());
    }

    /**
     * Creates an OpenAPI specification instance from the given URL.
     *
     * @param specUrl                 the URL pointing to the OpenAPI specification to load
     * @param openApiValidationPolicy the validation policy to apply to the loaded OpenApi
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification from(URL specUrl,
        OpenApiValidationPolicy openApiValidationPolicy) {
        OpenApiSpecification specification = new OpenApiSpecification(openApiValidationPolicy);
        OasDocument openApiDoc;
        OpenApiValidationContext openApiValidationContext;
        if (specUrl.getProtocol().startsWith(HTTPS)) {
            openApiDoc = OpenApiResourceLoader.fromSecuredWebResource(specUrl);
        } else {
            openApiDoc = OpenApiResourceLoader.fromWebResource(specUrl);
        }

        openApiValidationContext = OpenApiValidationContextLoader.fromSpec(OasModelHelper.toJson(openApiDoc), openApiValidationPolicy);
        specification.setOpenApiValidationContext(openApiValidationContext);

        specification.setSpecUrl(specUrl.toString());
        specification.initPathLookups();
        specification.setOpenApiDoc(openApiDoc);
        specification.setRequestUrl(
            format("%s://%s%s%s", specUrl.getProtocol(), specUrl.getHost(),
                specUrl.getPort() > 0 ? ":" + specUrl.getPort() : "",
                specification.rootContextPath));

        return specification;
    }

    /**
     * Creates an OpenAPI specification instance from the specified resource, applying the default
     * validation strategy.
     *
     * @param resource the file resource containing the OpenAPI specification to load
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification from(Resource resource) {
        return from(resource, OpenApiSettings.getOpenApiValidationPolicy());
    }

    /**
     * Creates an OpenAPI specification instance from the specified resource.
     *
     * @param resource                the file resource containing the OpenAPI specification to
     *                                load
     * @param openApiValidationPolicy the validation policy to apply to the loaded OpenApi
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification from(Resource resource,
        OpenApiValidationPolicy openApiValidationPolicy) {
        OpenApiSpecification specification = new OpenApiSpecification(openApiValidationPolicy);

        OasDocument openApiDoc = OpenApiResourceLoader.fromFile(resource);

        specification.setOpenApiValidationContext(
            OpenApiValidationContextLoader.fromSpec(OasModelHelper.toJson(openApiDoc), openApiValidationPolicy));
        specification.setOpenApiDoc(openApiDoc);

        String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
            .orElse(singletonList(HTTP))
            .stream()
            .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
            .findFirst()
            .orElse(HTTP);

        specification.setSpecUrl(resource.getLocation());
        specification.setRequestUrl(
            format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                getBasePath(openApiDoc)));

        return specification;
    }

    /**
     * Creates an OpenAPI specification instance from the provided OpenAPI specification string.
     *
     * @param openApi the OpenAPI specification content as a string
     * @return an OpenApiSpecification instance populated with the document and validation context
     */
    public static OpenApiSpecification fromString(String openApi) {
        OpenApiSpecification specification = new OpenApiSpecification();
        OasDocument openApiDoc = OpenApiResourceLoader.fromString(openApi);

        specification.setOpenApiDoc(openApiDoc);
        specification.setOpenApiValidationContext(
            OpenApiValidationContextLoader.fromString(openApi));

        String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
            .orElse(singletonList(HTTP))
            .stream()
            .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
            .findFirst()
            .orElse(HTTP);

        specification.setSpecUrl("loaded from memory");
        specification.setRequestUrl(
            format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                specification.rootContextPath));

        return specification;
    }

    /**
     * Get the UID of this specification.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Get the unique id of the given operation.
     */
    @SuppressWarnings("unused")
    public String getUniqueId(OasOperation oasOperation) {
        return operationToUniqueId.get(oasOperation);
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
                    initApiDoc(() -> OpenApiResourceLoader.fromSecuredWebResource(specWebResource));
                } else {
                    initApiDoc(() -> OpenApiResourceLoader.fromWebResource(specWebResource));
                }

                if (requestUrl == null) {
                    setRequestUrl(format("%s://%s%s%s", specWebResource.getProtocol(),
                        specWebResource.getHost(),
                        specWebResource.getPort() > 0 ? ":" + specWebResource.getPort() : "",
                        getBasePath(openApiDoc)));
                }

            } else {
                Resource resource = Resources.create(resolvedSpecUrl);
                initApiDoc(() -> OpenApiResourceLoader.fromFile(resource));


                if (requestUrl == null) {
                    String schemeToUse = Optional.ofNullable(OasModelHelper.getSchemes(openApiDoc))
                        .orElse(singletonList(HTTP))
                        .stream()
                        .filter(s -> s.equals(HTTP) || s.equals(HTTPS))
                        .findFirst()
                        .orElse(HTTP);

                    setRequestUrl(
                        format("%s://%s%s", schemeToUse, OasModelHelper.getHost(openApiDoc),
                            getBasePath(openApiDoc)));
                }
            }
        }

        setOpenApiValidationContext(
            OpenApiValidationContextLoader.fromSpec(OasModelHelper.toJson(openApiDoc),
                openApiValidationPolicy));

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

        determineUid();

        operationIdToOperationPathAdapter.clear();
        OasModelHelper.visitOasOperations(this.openApiDoc, (oasPathItem, oasOperation) -> {
            String path = oasPathItem.getPath();

            if (isEmpty(path)) {
                logger.warn("Skipping path item without path.");
                return;
            }

            for (Map.Entry<String, OasOperation> operationEntry : OasModelHelper.getOperationMap(
                oasPathItem).entrySet()) {
                storeOperationPathAdapter(operationEntry.getValue(), oasPathItem);
            }
        });
    }

    private void determineUid() {
        if (uid != null) {
            aliases.remove(uid);
        }
        uid = DigestUtils.sha256Hex(OasModelHelper.toJson(openApiDoc) + getFullContextPath());
        aliases.add(uid);
    }

    /**
     * Stores an {@link OperationPathAdapter} in
     * {@link org.citrusframework.openapi.OpenApiSpecification#operationIdToOperationPathAdapter}.
     * The adapter is stored using two keys: the operationId (optional) and the full path of the
     * operation, including the method. The full path is always determinable and thus can always be
     * safely used.
     *
     * @param operation The {@link OperationPathAdapter} to store.
     * @param pathItem  The path item of the operation, including the method.
     */
    private void storeOperationPathAdapter(OasOperation operation, OasPathItem pathItem) {

        String fullContextPath = getFullContextPath();
        String fullOperationPath = getFullPath(pathItem);
        String path = pathItem.getPath();

        String uniqueOperationId = OpenApiUtils.createFullPathOperationIdentifier(operation,
            fullOperationPath);
        operationToUniqueId.put(operation, uniqueOperationId);

        OperationPathAdapter operationPathAdapter = new OperationPathAdapter(path, fullContextPath,
            appendSegmentToUrlPath(fullContextPath, path), operation, uniqueOperationId);

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

    public String getRootContextPath() {
        return rootContextPath;
    }

    /**
     * Sets the root context path for the OpenAPI integration.
     * <p>
     * This path will be prepended to the base path when constructing the full URL path. After
     * setting the root context path, the internal path lookups are re-initialized to reflect the
     * updated configuration.
     * </p>
     * <p><b>Side Effect:</b> Invokes {@link #initPathLookups()} to update internal path mappings
     * based on the new root context path.</p>
     *
     * @param rootContextPath the root context path to set
     * @see #rootContextPath for more details on how this path is used
     * @see #initPathLookups() for the re-initialization of path lookups
     */
    public void setRootContextPath(String rootContextPath) {
        this.rootContextPath = rootContextPath;
        initPathLookups();
    }

    public OpenApiSpecification rootContextPath(String rootContextPath) {
        setRootContextPath(rootContextPath);
        return this;
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
        initOpenApiDoc(context);

        return Optional.ofNullable(operationIdToOperationPathAdapter.get(operationId));
    }

    public void initOpenApiDoc(TestContext context) {
        if (openApiDoc == null) {
            getOpenApiDoc(context);
        }
    }

    /**
     * Get the full path for the given {@link OasPathItem}.
     * <p>
     * The full path is constructed by concatenating the root context path, the base path (if
     * applicable), and the path of the given {@code oasPathItem}. The resulting format is:
     * </p>
     * <pre>
     * /rootContextPath/basePath/pathItemPath
     * </pre>
     * If the base path is to be neglected, it is excluded from the final constructed path.
     *
     * @param oasPathItem the OpenAPI path item whose full path is to be constructed
     * @return the full URL path, consisting of the root context path, base path, and the given path
     * item
     */
    public String getFullPath(OasPathItem oasPathItem) {
        return appendSegmentToUrlPath(rootContextPath,
                getFullBasePath(oasPathItem));
    }

    /**
     * Get the full base-path for the given {@link OasPathItem}.
     * <p>
     * The full base-path is constructed by concatenating the base path (if
     * applicable), and the path of the given {@code oasPathItem}. The resulting format is:
     * </p>
     * <pre>
     * /basePath/pathItemPath
     * </pre>
     * If the base path is to be neglected, it is excluded from the final constructed path.
     *
     * @param oasPathItem the OpenAPI path item whose full base-path is to be constructed
     * @return the full base URL path, consisting of the base path, and the given path
     * item
     */
    public String getFullBasePath(OasPathItem oasPathItem) {
        return appendSegmentToUrlPath(
                getApplicableBasePath(), oasPathItem.getPath());
    }


    /**
     * Constructs the full context path for the given {@link OasPathItem}.
     * <p>
     * The full context path is constructed by appending the root context path to the base path
     * specified in the OpenAPI document. If the base path should be neglected (as indicated by
     * {@link #neglectBasePath}), only the root context path will be used.
     * </p>
     *
     * @return the full context path, consisting of the root context path and optionally the base
     * path
     * @see #neglectBasePath to understand when the base path is omitted
     * @see #rootContextPath for the field used as the root context path
     */
    public String getFullContextPath() {
        return appendSegmentToUrlPath(rootContextPath,
            getApplicableBasePath());
    }

    /**
     * Sets whether the base path should be excluded when constructing the full operation path.
     *
     * <p><b>Side Effect:</b> Invokes {@link #initPathLookups()} to update internal path mappings
     * based on the new root context path.</p>
     *
     * @param neglectBasePath {@code true} to exclude the base path, {@code false} to include it
     * @see #neglectBasePath for the field description
     * @see #getFullPath(OasPathItem) for the method affected by this flag
     * @see #initPathLookups() for the re-initialization of path lookups
     */
    public void setNeglectBasePath(boolean neglectBasePath) {
        this.neglectBasePath = neglectBasePath;
        initPathLookups();
    }

    public OpenApiSpecification neglectBasePath(boolean neglectBasePath) {
        setNeglectBasePath(neglectBasePath);
        return this;
    }

    /**
     * Gets the base path if basePath should be applied.
     */
    private String getApplicableBasePath() {
        return neglectBasePath ? "" : getBasePath(openApiDoc);
    }


    /**
     * Add another alias for this specification.
     */
    public OpenApiSpecification alias(String alias) {
        addAlias(alias);
        return this;
    }
}
