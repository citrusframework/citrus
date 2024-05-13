package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import org.citrusframework.testapi.ApiActionBuilderCustomizerService;
import org.citrusframework.openapi.generator.util.TestApiActionBuilderCustomizer;
import org.junit.jupiter.api.Test;

class ServiceLoaderTest {

    @Test
    void test() {
        ServiceLoader<ApiActionBuilderCustomizerService> serviceLoader = ServiceLoader.load(
            ApiActionBuilderCustomizerService.class, ApiActionBuilderCustomizerService.class.getClassLoader());
        List<Provider<ApiActionBuilderCustomizerService>> list = serviceLoader.stream().toList();
        assertThat(list).hasSize(1);
        ApiActionBuilderCustomizerService apiActionBuilderCustomizerService = list.iterator().next()
            .get();
        assertThat(apiActionBuilderCustomizerService).isInstanceOf(TestApiActionBuilderCustomizer.class);
    }
}
