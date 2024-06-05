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

package org.citrusframework.openapi.generator.rest.multiparttest.citrus.extension;

import org.citrusframework.openapi.generator.rest.multiparttest.request.MultiparttestControllerApi;
import org.citrusframework.openapi.generator.rest.multiparttest.citrus.MultipartTestBeanDefinitionParser;

import javax.annotation.processing.Generated;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
public class MultipartTestNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("deleteObjectRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.DeleteObjectRequest.class));
        registerBeanDefinitionParser("fileExistsRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.FileExistsRequest.class));
        registerBeanDefinitionParser("generateReportRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.GenerateReportRequest.class));
        registerBeanDefinitionParser("multipleDatatypesRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.MultipleDatatypesRequest.class));
        registerBeanDefinitionParser("postFileRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.PostFileRequest.class));
        registerBeanDefinitionParser("postRandomRequest", new MultipartTestBeanDefinitionParser(MultiparttestControllerApi.PostRandomRequest.class));
    }
}
