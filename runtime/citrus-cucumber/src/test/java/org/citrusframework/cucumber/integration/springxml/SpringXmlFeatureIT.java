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
package org.citrusframework.cucumber.integration.springxml;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.OBJECT_FACTORY_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runs XML step templates together with Spring-managed Java steps on the Citrus Spring object factory.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("org.citrusframework.cucumber.integration.springxml")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.citrusframework.cucumber.integration.springxml")
@ConfigurationParameter(key = OBJECT_FACTORY_PROPERTY_NAME, value = "org.citrusframework.cucumber.backend.spring.CitrusSpringObjectFactory")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,org.citrusframework.cucumber.CitrusReporter")
public class SpringXmlFeatureIT {
}
