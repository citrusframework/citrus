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

package org.citrusframework.cucumber.steps;

import org.citrusframework.cucumber.steps.http.HttpEndpointConfiguration;
import org.citrusframework.cucumber.steps.jms.JmsBrokerConfiguration;
import org.citrusframework.cucumber.steps.kafka.KafkaConfiguration;
import org.citrusframework.cucumber.steps.knative.KnativeServiceConfiguration;
import org.citrusframework.cucumber.steps.kubernetes.KubernetesServiceConfiguration;
import org.citrusframework.cucumber.steps.openapi.OpenApiPetstoreConfiguration;
import org.citrusframework.cucumber.steps.selenium.SeleniumConfiguration;
import org.citrusframework.cucumber.steps.standard.StandardEndpointConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    StandardEndpointConfiguration.class,
    HttpEndpointConfiguration.class,
    KubernetesServiceConfiguration.class,
    KnativeServiceConfiguration.class,
    KafkaConfiguration.class,
    JmsBrokerConfiguration.class,
    OpenApiPetstoreConfiguration.class,
    SeleniumConfiguration.class,
})
public class EndpointConfig {
}
