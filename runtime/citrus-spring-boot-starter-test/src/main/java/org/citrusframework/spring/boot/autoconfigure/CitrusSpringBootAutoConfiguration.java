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

import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.spring.context.TestContextFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;

/**
 * Auto-configures Citrus infrastructure for Spring Boot tests.
 */
@AutoConfiguration
@ConditionalOnClass(CitrusSpringConfig.class)
@ConditionalOnProperty(prefix = "citrus.spring.boot", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(TestContextFactoryBean.class)
@Import(CitrusSpringConfig.class)
public class CitrusSpringBootAutoConfiguration {
}
