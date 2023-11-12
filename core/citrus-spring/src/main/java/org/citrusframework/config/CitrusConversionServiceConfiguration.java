/*
 * Copyright 2023 the original author or authors.
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
package org.citrusframework.config;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

/**
 * Optional configuration providing a {@link org.springframework.core.convert.ConversionService} with Citrus-related converters.
 * <p>
 * This configuration is necessary only when using Spring without Spring Boot. Spring Boot includes a standard conversion service
 * that automatically detects and uses relevant converters by default.
 *
 * @author Thorsten Schlathoelter
 * @since 4.0
 */
@Configuration
public class CitrusConversionServiceConfiguration {

    @Bean(name = "conversionService")
    public ConversionServiceFactoryBean conversionService(Set<Converter<?,?>> converters) {
        ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
        conversionServiceFactoryBean.setConverters(converters);
        return conversionServiceFactoryBean;
    }

}
