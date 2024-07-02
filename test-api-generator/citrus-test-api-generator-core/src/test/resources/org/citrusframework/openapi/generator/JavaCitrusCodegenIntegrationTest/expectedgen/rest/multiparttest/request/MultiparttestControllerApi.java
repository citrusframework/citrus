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

package org.citrusframework.openapi.generator.rest.multiparttest.request;

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

import org.citrusframework.openapi.generator.rest.multiparttest.citrus.MultipartTestAbstractTestRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class MultiparttestControllerApi implements GeneratedApi
{

    public static final MultiparttestControllerApi INSTANCE = new MultiparttestControllerApi();

    public String getApiTitle() {
        return "multiparttest API";
    }

    public String getApiVersion() {
        return "2.0.0";
    }

    public String getApiPrefix() {
        return "MultipartTest";
    }

    public Map<String,String> getApiInfoExtensions() {
        Map<String, String> infoExtensionMap = new HashMap<>();
        infoExtensionMap.put("x-citrus-api-name", "multiparttest-rest-resource");
        infoExtensionMap.put("x-citrus-app", "MPT");
        return infoExtensionMap;
    }

    /** deleteObject (DELETE /api/v2/multitest-file/{bucket}/{filename})
        Delete file.
        
    **/
    public static class DeleteObjectRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-file/{bucket}/{filename}";
        private final Logger coverageLogger = LoggerFactory.getLogger(DeleteObjectRequest.class);

        private String bucket;

        private String filename;

        
        public DeleteObjectRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":deleteObjectRequestType");
        }

        public String getOperationName() {
            return "deleteObject";
        }

        public String getMethod() {
            return "DELETE";
        }

        public String getPath() {
            return "/api/v2/multitest-file/{bucket}/{filename}";
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

            coverageLogger.trace(coverageMarker, "deleteObject;DELETE;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "bucket" + "}", bucket);endpoint = endpoint.replace("{" + "filename" + "}", filename);
            return endpoint;
        }
    }
    /** fileExists (GET /api/v2/multitest-file/{bucket}/{filename}/exists)
        Checks if file exist.
        
    **/
    public static class FileExistsRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-file/{bucket}/{filename}/exists";
        private final Logger coverageLogger = LoggerFactory.getLogger(FileExistsRequest.class);

        private String bucket;

        private String filename;

        
        public FileExistsRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":fileExistsRequestType");
        }

        public String getOperationName() {
            return "fileExists";
        }

        public String getMethod() {
            return "GET";
        }

        public String getPath() {
            return "/api/v2/multitest-file/{bucket}/{filename}/exists";
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

            coverageLogger.trace(coverageMarker, "fileExists;GET;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "bucket" + "}", bucket);endpoint = endpoint.replace("{" + "filename" + "}", filename);
            return endpoint;
        }
    }
    /** generateReport (POST /api/v2/multitest-reportgeneration)
        summary
        
    **/
    public static class GenerateReportRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-reportgeneration";
        private final Logger coverageLogger = LoggerFactory.getLogger(GenerateReportRequest.class);

        private String template;

        private String additionalData;

        private String _schema;

        
        public GenerateReportRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":generateReportRequestType");
        }

        public String getOperationName() {
            return "generateReport";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/api/v2/multitest-reportgeneration";
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
            if(StringUtils.isBlank(template)) {
                throw new CitrusRuntimeException(String.format("Required attribute '%s' is not specified", "template"));
            }
            if (StringUtils.isNotBlank(template)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(template);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("template", resource);
                } else {
                    multiValues.add("template", template);
                }
                bodyLog += template.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }
            if (StringUtils.isNotBlank(additionalData)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(additionalData);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("additionalData", resource);
                } else {
                    multiValues.add("additionalData", additionalData);
                }
                bodyLog += additionalData.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }
            if (StringUtils.isNotBlank(_schema)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(_schema);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("_schema", resource);
                } else {
                    multiValues.add("_schema", _schema);
                }
                bodyLog += _schema.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }

            bodyLog +=  "\";\"" + MediaType.MULTIPART_FORM_DATA_VALUE + "\"";
            messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .body(multiValues);


            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "generateReport;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public void setAdditionalData(String additionalData) {
            this.additionalData = additionalData;
        }

        public void set_schema(String _schema) {
            this._schema = _schema;
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** multipleDatatypes (POST /api/v2/multitest-multipledatatypes)
        summary
        
    **/
    public static class MultipleDatatypesRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-multipledatatypes";
        private final Logger coverageLogger = LoggerFactory.getLogger(MultipleDatatypesRequest.class);

        private String stringData;

        private String booleanData;

        private String integerData;

        
        public MultipleDatatypesRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":multipleDatatypesRequestType");
        }

        public String getOperationName() {
            return "multipleDatatypes";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/api/v2/multitest-multipledatatypes";
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
            if (StringUtils.isNotBlank(stringData)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(stringData);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("stringData", resource);
                } else {
                    multiValues.add("stringData", stringData);
                }
                bodyLog += stringData.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }
            if (StringUtils.isNotBlank(booleanData)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(booleanData);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("booleanData", resource);
                } else {
                    multiValues.add("booleanData", booleanData);
                }
                bodyLog += booleanData.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }
            if (StringUtils.isNotBlank(integerData)) {
                // first try to load from resource
                ClassPathResource resource = null;
                try {
                     resource = new ClassPathResource(integerData);
                }
                catch(Exception ignore) {
                    // Use plain text instead of resource
                }

                if(resource != null && resource.exists()){
                    multiValues.add("integerData", resource);
                } else {
                    multiValues.add("integerData", integerData);
                }
                bodyLog += integerData.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }

            bodyLog +=  "\";\"" + MediaType.MULTIPART_FORM_DATA_VALUE + "\"";
            messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .body(multiValues);


            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "multipleDatatypes;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setStringData(String stringData) {
            this.stringData = stringData;
        }

        public void setBooleanData(String booleanData) {
            this.booleanData = booleanData;
        }

        public void setIntegerData(String integerData) {
            this.integerData = integerData;
        }
        
        private String replacePathParams(String endpoint) {
            
            return endpoint;
        }
    }
    /** postFile (POST /api/v2/multitest-file/{bucket}/{filename})
        Uploads file.
        
    **/
    public static class PostFileRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-file/{bucket}/{filename}";
        private final Logger coverageLogger = LoggerFactory.getLogger(PostFileRequest.class);

        private String bucket;

        private String filename;

        private String multipartFile;

        
        public PostFileRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":postFileRequestType");
        }

        public String getOperationName() {
            return "postFile";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/api/v2/multitest-file/{bucket}/{filename}";
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
            if (StringUtils.isNotBlank(multipartFile)) {
                multiValues.add("multipartFile", new ClassPathResource(multipartFile));
                bodyLog += multipartFile.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") +",";
            }

            bodyLog +=  "\";\"" + MediaType.MULTIPART_FORM_DATA_VALUE + "\"";
            messageBuilderSupport.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .body(multiValues);


            Map<String, String> queryParams = new HashMap<>();
            
            String query = queryParams.entrySet().stream().map(e -> "\"" + e.getKey() + "\":\"" + e.getValue() + "\"").collect(Collectors.joining(",", "{", "}"));
            
            httpClientRequestActionBuilder.withReferenceResolver(context.getReferenceResolver());
            httpClientRequestActionBuilder = customizeBuilder(INSTANCE, context, httpClientRequestActionBuilder);

            httpClientRequestActionBuilder.build().execute(context);

            coverageLogger.trace(coverageMarker, "postFile;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public void setMultipartFile(String multipartFile) {
            this.multipartFile = multipartFile;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "bucket" + "}", bucket);endpoint = endpoint.replace("{" + "filename" + "}", filename);
            return endpoint;
        }
    }
    /** postRandom (POST /api/v2/multitest-file/{bucket}/{filename}/random)
        Uploads random file.
        
    **/
    public static class PostRandomRequest extends MultipartTestAbstractTestRequest implements GeneratedApiRequest {

        private static final String ENDPOINT = "/api/v2/multitest-file/{bucket}/{filename}/random";
        private final Logger coverageLogger = LoggerFactory.getLogger(PostRandomRequest.class);

        private String bucket;

        private String filename;

        
        public PostRandomRequest() {
            // The name will be overwritten with the tag name using the actual namespace as prefix, when the class is loaded from xml
            setName("MultipartTest".toLowerCase() + ":postRandomRequestType");
        }

        public String getOperationName() {
            return "postRandom";
        }

        public String getMethod() {
            return "POST";
        }

        public String getPath() {
            return "/api/v2/multitest-file/{bucket}/{filename}/random";
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

            coverageLogger.trace(coverageMarker, "postRandom;POST;\"" +
                            query.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\"\"") + "\";\"" +
                            bodyLog);
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
        
        private String replacePathParams(String endpoint) {
            endpoint = endpoint.replace("{" + "bucket" + "}", bucket);endpoint = endpoint.replace("{" + "filename" + "}", filename);
            return endpoint;
        }
    }
}
