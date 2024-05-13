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
