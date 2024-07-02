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

/**
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.rest.petstore.request;

import jakarta.annotation.Generated;
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

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class UserApi implements GeneratedApi
{

    public static final UserApi INSTANCE = new UserApi();

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

    /** createUser (POST /user)
        Create user
        
    **/
    public static class CreateUserRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user";
        private final Logger coverageLogger = LoggerFactory.getLogger(CreateUserRequest.class);

        
        public CreateUserRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":createUserRequestType");
        }

        public String getOperationName() {
            return "createUser";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/user";
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

            coverageLogger.trace(coverageMarker, "createUser;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** createUsersWithArrayInput (POST /user/createWithArray)
        Creates list of users with given input array
        
    **/
    public static class CreateUsersWithArrayInputRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/createWithArray";
        private final Logger coverageLogger = LoggerFactory.getLogger(CreateUsersWithArrayInputRequest.class);

        
        public CreateUsersWithArrayInputRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":createUsersWithArrayInputRequestType");
        }

        public String getOperationName() {
            return "createUsersWithArrayInput";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/user/createWithArray";
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

            coverageLogger.trace(coverageMarker, "createUsersWithArrayInput;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** createUsersWithListInput (POST /user/createWithList)
        Creates list of users with given input array
        
    **/
    public static class CreateUsersWithListInputRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/createWithList";
        private final Logger coverageLogger = LoggerFactory.getLogger(CreateUsersWithListInputRequest.class);

        
        public CreateUsersWithListInputRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":createUsersWithListInputRequestType");
        }

        public String getOperationName() {
            return "createUsersWithListInput";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/user/createWithList";
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

            coverageLogger.trace(coverageMarker, "createUsersWithListInput;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** deleteUser (DELETE /user/{username})
        Delete user
        
    **/
    public static class DeleteUserRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/{username}";
        private final Logger coverageLogger = LoggerFactory.getLogger(DeleteUserRequest.class);

        private String username;

        
        public DeleteUserRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":deleteUserRequestType");
        }

        public String getOperationName() {
            return "deleteUser";
        }

        public String getMethod() {
            return "DELETE";
        }

        public String getPath() {
            return "/user/{username}";
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

            coverageLogger.trace(coverageMarker, "deleteUser;DELETE;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setUsername(String username) {
            this.username = username;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "username" + "}", username);
            return endpoint;
        }
    }
    /** getUserByName (GET /user/{username})
        Get user by user name
        
    **/
    public static class GetUserByNameRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/{username}";
        private final Logger coverageLogger = LoggerFactory.getLogger(GetUserByNameRequest.class);

        private String username;

        
        public GetUserByNameRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":getUserByNameRequestType");
        }

        public String getOperationName() {
            return "getUserByName";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/user/{username}";
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
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "getUserByName;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setUsername(String username) {
            this.username = username;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "username" + "}", username);
            return endpoint;
        }
    }
    /** loginUser (GET /user/login)
        Logs user into the system
        
    **/
    public static class LoginUserRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/login";
        private final Logger coverageLogger = LoggerFactory.getLogger(LoginUserRequest.class);

        private String username;

        private String password;

        
        public LoginUserRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":loginUserRequestType");
        }

        public String getOperationName() {
            return "loginUser";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/user/login";
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
            

            if (StringUtils.isNotBlank(this.username)) {
                queryParams.put("username", context.replaceDynamicContentInString(this.username));
                httpClientRequestActionBuilder.queryParam("username", this.username);
            }
            

            if (StringUtils.isNotBlank(this.password)) {
                queryParams.put("password", context.replaceDynamicContentInString(this.password));
                httpClientRequestActionBuilder.queryParam("password", this.password);
            }
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "loginUser;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** logoutUser (GET /user/logout)
        Logs out current logged in user session
        
    **/
    public static class LogoutUserRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/logout";
        private final Logger coverageLogger = LoggerFactory.getLogger(LogoutUserRequest.class);

        
        public LogoutUserRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":logoutUserRequestType");
        }

        public String getOperationName() {
            return "logoutUser";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/user/logout";
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
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "logoutUser;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** updateUser (PUT /user/{username})
        Updated user
        
    **/
    public static class UpdateUserRequest extends PetStoreAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/user/{username}";
        private final Logger coverageLogger = LoggerFactory.getLogger(UpdateUserRequest.class);

        private String username;

        
        public UpdateUserRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("PetStore".toLowerCase() + ":updateUserRequestType");
        }

        public String getOperationName() {
            return "updateUser";
        }

        public String getMethod() {
            return "PUT";
        }

        public String getPath() {
            return "/user/{username}";
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

            coverageLogger.trace(coverageMarker, "updateUser;PUT;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setUsername(String username) {
            this.username = username;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "username" + "}", username);
            return endpoint;
        }
    }
}
