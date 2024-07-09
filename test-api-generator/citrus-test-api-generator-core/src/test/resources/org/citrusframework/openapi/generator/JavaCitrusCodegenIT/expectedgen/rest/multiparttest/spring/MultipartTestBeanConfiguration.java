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

package org.citrusframework.openapi.generator.rest.multiparttest.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.rest.multiparttest.request.MultiparttestControllerApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@jakarta.annotation.Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen", date = "2024-07-03T15:24:46.388350800+02:00[Europe/Zurich]", comments = "Generator version: 7.5.0")
public class MultipartTestBeanConfiguration {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.DeleteObjectRequest deleteObjectRequest() {
        return new MultiparttestControllerApi.DeleteObjectRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.FileExistsRequest fileExistsRequest() {
        return new MultiparttestControllerApi.FileExistsRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.GenerateReportRequest generateReportRequest() {
        return new MultiparttestControllerApi.GenerateReportRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.MultipleDatatypesRequest multipleDatatypesRequest() {
        return new MultiparttestControllerApi.MultipleDatatypesRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.PostFileRequest postFileRequest() {
        return new MultiparttestControllerApi.PostFileRequest();
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public MultiparttestControllerApi.PostRandomRequest postRandomRequest() {
        return new MultiparttestControllerApi.PostRandomRequest();
    }
}
