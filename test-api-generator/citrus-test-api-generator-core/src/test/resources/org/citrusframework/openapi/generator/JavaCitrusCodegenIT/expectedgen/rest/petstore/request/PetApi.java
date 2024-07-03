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

package org.citrusframework.openapi.generator.rest.petstore.request;

import org.citrusframework.testapi.GeneratedApi;
import org.citrusframework.testapi.GeneratedApiRequest;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.http.actions.HttpActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.citrusframework.openapi.generator.rest.petstore.citrus.PetStoreAbstractTestRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:45.610010900+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class PetApi implements GeneratedApi
{

    public static final PetApi INSTANCE = new PetApi();

    public String getApiTitle() {
        return "OpenAPI Petstore";
    }

    public String getApiVersion() {
        return "1.0.0";
    }

    public String getApiPrefix() {
        return "PetStore";
    }

    public Map<String,String> getApiInfoExtensions() {
        Map<String, String> infoExtensionMap = new HashMap<>();
        infoExtensionMap.put("x-citrus-api-name", "petstore");
        infoExtensionMap.put("x-citrus-app", "PETS");
        return infoExtensionMap;
    }

    /** addPet (POST /pet)
        Add a new pet to the store
        
    **/
    public static class AddPetRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet";
        private final Logger coverageLogger = LoggerFactory.getLogger(AddPetRequest.class);

        
        public AddPetRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":addPetRequestType");
        }

        public String getOperationName() {
            return "addPet";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/pet";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .post(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "addPet;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** deletePet (DELETE /pet/{petId})
        Deletes a pet
        
    **/
    public static class DeletePetRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/{petId}";
        private final Logger coverageLogger = LoggerFactory.getLogger(DeletePetRequest.class);

        private String petId;

        
        public DeletePetRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":deletePetRequestType");
        }

        public String getOperationName() {
            return "deletePet";
        }

        public String getMethod() {
            return "DELETE";
        }

        public String getPath() {
            return "/pet/{petId}";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .delete(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "deletePet;DELETE;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setPetId(String petId) {
            this.petId = petId;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "petId" + "}", petId);
            return endpoint;
        }
    }
    /** findPetsByStatus (GET /pet/findByStatus)
        Finds Pets by status
        
    **/
    public static class FindPetsByStatusRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/findByStatus";
        private final Logger coverageLogger = LoggerFactory.getLogger(FindPetsByStatusRequest.class);

        private String status;

        
        public FindPetsByStatusRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":findPetsByStatusRequestType");
        }

        public String getOperationName() {
            return "findPetsByStatus";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/pet/findByStatus";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .get(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            

            if (StringUtils.isNotBlank(this.status)) {
                queryParams.put("status", context.replaceDynamicContentInString(this.status));
                httpClientRequestActionBuilder.queryParam("status", this.status);
            }
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "findPetsByStatus;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setStatus(String status) {
            this.status = status;
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** findPetsByTags (GET /pet/findByTags)
        Finds Pets by tags
        
    **/
    public static class FindPetsByTagsRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/findByTags";
        private final Logger coverageLogger = LoggerFactory.getLogger(FindPetsByTagsRequest.class);

        private String tags;

        
        public FindPetsByTagsRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":findPetsByTagsRequestType");
        }

        public String getOperationName() {
            return "findPetsByTags";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/pet/findByTags";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .get(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            

            if (StringUtils.isNotBlank(this.tags)) {
                queryParams.put("tags", context.replaceDynamicContentInString(this.tags));
                httpClientRequestActionBuilder.queryParam("tags", this.tags);
            }
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "findPetsByTags;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setTags(String tags) {
            this.tags = tags;
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** getPetById (GET /pet/{petId})
        Find pet by ID
        
    **/
    public static class GetPetByIdRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/{petId}";
        private final Logger coverageLogger = LoggerFactory.getLogger(GetPetByIdRequest.class);

        private String petId;

        
        @Value("${" + "petStoreEndpoint.basic.username:#{null}}")
        private String basicUsername;
        @Value("${" + "petStoreEndpoint.basic.password:#{null}}")
        private String basicPassword;


        public GetPetByIdRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":getPetByIdRequestType");
        }

        public String getOperationName() {
            return "getPetById";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/pet/{petId}";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .get(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            

            if(basicUsername != null && basicPassword != null){
                messageBuilderSupport.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((context.replaceDynamicContentInString(basicUsername)+":"+context.replaceDynamicContentInString(basicPassword)).getBytes()));
            }
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getPetById;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setPetId(String petId) {
            this.petId = petId;
        }
        

        public void setBasicUsername(String basicUsername) {
            this.basicUsername = basicUsername;
        }

        public void setBasicPassword(String basicPassword) {
            this.basicPassword = basicPassword;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "petId" + "}", petId);
            return endpoint;
        }
    }
    /** updatePet (PUT /pet)
        Update an existing pet
        
    **/
    public static class UpdatePetRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet";
        private final Logger coverageLogger = LoggerFactory.getLogger(UpdatePetRequest.class);

        
        public UpdatePetRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":updatePetRequestType");
        }

        public String getOperationName() {
            return "updatePet";
        }

        public String getMethod() {
            return "PUT";
        }

        public String getPath() {
            return "/pet";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .put(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "updatePet;PUT;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** updatePetWithForm (POST /pet/{petId})
        Updates a pet in the store with form data
        
    **/
    public static class UpdatePetWithFormRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/{petId}";
        private final Logger coverageLogger = LoggerFactory.getLogger(UpdatePetWithFormRequest.class);

        private String petId;

        
        public UpdatePetWithFormRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":updatePetWithFormRequestType");
        }

        public String getOperationName() {
            return "updatePetWithForm";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/pet/{petId}";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .post(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            String payload = null;
            String payloadType = null;
            if (StringUtils.isNotBlank(this.bodyFile)) {
                try {
                    payload = FileUtils.readToString(Resources.create(this.bodyFile), FileUtils.getDefaultCharset());
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Failed to read payload resource", e);
                }
                payloadType = this.bodyContentType;
            } else if (StringUtils.isNotBlank(this.bodyLiteral)) {
                payload = this.bodyLiteral;
                payloadType = this.bodyLiteralContentType;
            }
            String body = "";
            String bodyType = "";
            if(payload != null && payloadType != null) {
                messageBuilderSupport.body(payload).contentType(payloadType);
                body = context.replaceDynamicContentInString(payload);
                bodyType = context.replaceDynamicContentInString(payloadType);
            }

            bodyLog = body.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" + bodyType + "\"";

            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "updatePetWithForm;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setPetId(String petId) {
            this.petId = petId;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "petId" + "}", petId);
            return endpoint;
        }
    }
    /** uploadFile (POST /pet/{petId}/uploadImage)
        uploads an image
        
    **/
    public static class UploadFileRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/pet/{petId}/uploadImage";
        private final Logger coverageLogger = LoggerFactory.getLogger(UploadFileRequest.class);

        private String petId;

        private String additionalMetadata;

        private String _file;

        
        public UploadFileRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":uploadFileRequestType");
        }

        public String getOperationName() {
            return "uploadFile";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/pet/{petId}/uploadImage";
        }

        /**
        * This method sends the HTTP-Request
        */
        public void sendRequest(TestContext context) {
            HttpClientRequestActionBuilder httpClientRequestActionBuilder = new HttpActionBuilder().client(httpClient).send()
                .post(replacePathParams(ENDPOINT));

            HttpMessageBuilderSupport messageBuilderSupport = httpClientRequestActionBuilder.getMessageBuilderSupport();
            messageBuilderSupport.accept(responseAcceptType);

            if (cookies != null) {
                cookies.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
            }

            if (headers != null) {
                headers.forEach((k, v) -> messageBuilderSupport.cookie(new Cookie(k, v)));
                headers.forEach(messageBuilderSupport::header);
            }

            String bodyLog = "";
            MultiValueMap<String, Object> multiValues = new LinkedMultiValueMap<>();
            if (StringUtils.isNotBlank(additionalMetadata)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(additionalMetadata);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("additionalMetadata", resource);
                } else {
                    multiValues.add("additionalMetadata", additionalMetadata);
                }
                bodyLog += additionalMetadata.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }
            if (StringUtils.isNotBlank(_file)) {
                multiValues.add("_file", new ClassPathResource(_file));
                bodyLog += _file.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }

            bodyLog +=  "\";\"" + MediaType.MULTIPART_FORM_DATA_VALUE + "\"";
            messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .body(multiValues);


            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "uploadFile;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setPetId(String petId) {
            this.petId = petId;
        }

        public void setAdditionalMetadata(String additionalMetadata) {
            this.additionalMetadata = additionalMetadata;
        }

        public void set_file(String _file) {
            this._file = _file;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "petId" + "}", petId);
            return endpoint;
        }
    }
}
