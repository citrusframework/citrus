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

package org.citrusframework.spring.boot.autoconfigure;

import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.spring.context.TestContextFactoryBean;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.TypeConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CitrusSpringBootAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CitrusSpringBootAutoConfiguration.class));

    @Test
    void autoConfiguresCitrusInfrastructure() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TestContextFactoryBean.class);
            assertThat(context).hasSingleBean(EndpointFactory.class);
            assertThat(context).hasSingleBean(ReferenceResolver.class);
            assertThat(context).hasSingleBean(TypeConverter.class);
        });
    }

    @Test
    void backsOffWhenCitrusSpringConfigIsPresent() {
        contextRunner.withUserConfiguration(CitrusSpringConfig.class)
                .run(context -> assertThat(context.getBeanNamesForType(TestContextFactoryBean.class))
                        .containsExactly("&citrusTestContextFactory"));
    }

    @Test
    void canBeDisabled() {
        contextRunner.withPropertyValues("citrus.spring.boot.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TestContextFactoryBean.class);
                    assertThat(context).doesNotHaveBean(EndpointFactory.class);
                });
    }
}
