/** 
 * ==================================================
 * GENERATED CLASS, ALL CHANGES WILL BE LOST
 * ==================================================
 */

package org.citrusframework.openapi.generator.rest.multiparttest.spring;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.citrusframework.openapi.generator.rest.multiparttest.request.MultiparttestControllerApi;
import javax.annotation.processing.Generated;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@Generated(value = "org.citrusframework.openapi.generator.JavaCitrusCodegen")
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
